package com.wanshu.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanshu.common.config.CozeConfig;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Coze API 调用封装
 * <p>
 * 支持普通请求和 SSE 流式请求两种模式。
 * <p>
 * 非流式（chat）调用流程（Coze /v3/chat 为异步接口）：
 * 1. POST /v3/chat          → 获取 chat_id + conversation_id，status=in_progress
 * 2. POST /v3/chat/retrieve → 轮询直至 status=completed
 * 3. GET  /v3/chat/message/list → 取 role=assistant,type=answer 的消息正文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CozeUtil {

    private final CozeConfig cozeConfig;
    private final ObjectMapper objectMapper;

    /** 轮询间隔（毫秒） */
    private static final int POLL_INTERVAL_MS = 1_000;
    /** 最大轮询次数（默认 60 次 = 60 秒） */
    private static final int MAX_POLL_TIMES = 60;

    // ─────────────────────────── 公共方法 ───────────────────────────

    /** 非流式对话：内部执行「发起 → 轮询 → 取消息」三步，阻塞直到 Bot 回复完成。 */
    public String chat(String botId, String userId, String query) {
        try {
            RestTemplate rest = buildRestTemplate();
            HttpHeaders headers = buildAuthHeaders();

            // Step 1：发起对话
            Map<String, Object> body = new HashMap<>();
            body.put("bot_id", botId);
            body.put("user_id", userId);
            body.put("stream", false);
            body.put("auto_save_history", true);
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", query);
            msg.put("content_type", "text");
            body.put("additional_messages", new Object[]{msg});

            ResponseEntity<String> resp = rest.exchange(
                    cozeConfig.getApiBaseUrl() + "/v3/chat",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class);

            JsonNode root = objectMapper.readTree(resp.getBody());
            if (!isCozeSuccess(root)) {
                log.warn("Coze /v3/chat 发起失败: {}", resp.getBody());
                throw new BusinessException(ResultCode.COZE_API_ERROR);
            }

            JsonNode data = root.path("data");
            String chatId = data.path("id").asText("");
            String conversationId = data.path("conversation_id").asText("");

            if (!StringUtils.hasText(chatId) || !StringUtils.hasText(conversationId)) {
                log.warn("Coze /v3/chat 未返回 chat_id / conversation_id: {}", resp.getBody());
                throw new BusinessException(ResultCode.COZE_API_ERROR);
            }

            // Step 2：轮询直至完成
            String status = data.path("status").asText("in_progress");
            for (int i = 0; i < MAX_POLL_TIMES && "in_progress".equals(status); i++) {
                Thread.sleep(POLL_INTERVAL_MS);
                status = retrieveStatus(rest, headers, conversationId, chatId);
                log.debug("Coze 轮询 [{}/{}] status={}", i + 1, MAX_POLL_TIMES, status);
            }

            if (!"completed".equals(status)) {
                log.warn("Coze 对话未完成，最终状态={}, chatId={}", status, chatId);
                throw new BusinessException("AI 客服响应超时，请稍后重试");
            }

            // Step 3：获取消息列表，取 role=assistant type=answer 的正文
            return fetchAssistantAnswer(rest, headers, conversationId, chatId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Coze 非流式对话失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.COZE_API_ERROR);
        }
    }

    /** 流式对话（SSE），逐段推送回复到前端 */
    public void chatStream(String botId, String userId, String query, SseEmitter emitter) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(cozeConfig.getApiBaseUrl() + "/v3/chat");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + cozeConfig.getAccessToken());
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);
            connection.setConnectTimeout(cozeConfig.getTimeout() * 1000);
            connection.setReadTimeout(cozeConfig.getTimeout() * 1000);

            Map<String, Object> body = new HashMap<>();
            body.put("bot_id", botId);
            body.put("user_id", userId);
            body.put("stream", true);
            body.put("auto_save_history", true);
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", query);
            msg.put("content_type", "text");
            body.put("additional_messages", new Object[]{msg});

            String jsonBody = objectMapper.writeValueAsString(body);
            connection.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String eventData = line.substring(5).trim();
                        if ("[DONE]".equals(eventData)) {
                            emitter.complete();
                            return;
                        }
                        try {
                            JsonNode event = objectMapper.readTree(eventData);
                            String eventType = event.path("event").asText("");
                            if ("conversation.message.delta".equals(eventType)) {
                                String content = event.path("data").path("content").asText("");
                                if (!content.isEmpty()) {
                                    emitter.send(SseEmitter.event().data(content));
                                }
                            } else if ("conversation.message.completed".equals(eventType)) {
                                emitter.complete();
                                return;
                            }
                        } catch (Exception ex) {
                            log.debug("解析 SSE 事件跳过: {}", eventData);
                        }
                    }
                }
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("Coze SSE 流式调用失败: {}", e.getMessage(), e);
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // ─────────────────────────── 快捷入口 ───────────────────────────

    public String customerChat(String userId, String query) {
        return chat(cozeConfig.getCustomerBotId(), userId, query);
    }

    public void customerChatStream(String userId, String query, SseEmitter emitter) {
        chatStream(cozeConfig.getCustomerBotId(), userId, query, emitter);
    }

    public String dispatchChat(String userId, String query) {
        return chat(cozeConfig.getDispatchBotId(), userId, query);
    }

    public void dispatchChatStream(String userId, String query, SseEmitter emitter) {
        chatStream(cozeConfig.getDispatchBotId(), userId, query, emitter);
    }

    // ─────────────────────────── 私有工具 ───────────────────────────

    /**
     * 轮询 /v3/chat/retrieve，返回当前 status 字符串。
     */
    private String retrieveStatus(RestTemplate rest, HttpHeaders headers,
                                   String conversationId, String chatId) throws Exception {
        String url = cozeConfig.getApiBaseUrl()
                + "/v3/chat/retrieve?conversation_id=" + conversationId + "&chat_id=" + chatId;
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        JsonNode root = objectMapper.readTree(resp.getBody());
        if (!isCozeSuccess(root)) {
            log.warn("Coze /v3/chat/retrieve 返回异常: {}", resp.getBody());
            return "failed";
        }
        return root.path("data").path("status").asText("in_progress");
    }

    /**
     * 调用 /v3/chat/message/list，提取 role=assistant、type=answer 的最后一条消息正文。
     */
    private String fetchAssistantAnswer(RestTemplate rest, HttpHeaders headers,
                                         String conversationId, String chatId) throws Exception {
        String url = cozeConfig.getApiBaseUrl()
                + "/v3/chat/message/list?conversation_id=" + conversationId + "&chat_id=" + chatId;
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        JsonNode root = objectMapper.readTree(resp.getBody());
        log.debug("Coze message/list 响应: {}", resp.getBody());

        if (!isCozeSuccess(root)) {
            log.warn("Coze /v3/chat/message/list 返回异常: {}", resp.getBody());
            return "";
        }

        // data 可能是数组，也可能是 { items: [...] }
        JsonNode data = root.path("data");
        JsonNode items = data.isArray() ? data : data.path("items");

        String answer = "";
        if (items.isArray()) {
            for (JsonNode m : items) {
                String role = m.path("role").asText("");
                String type = m.path("type").asText("");
                // type=answer 是正文，type=follow_up 是追问（忽略）
                if ("assistant".equals(role) && ("answer".equals(type) || "".equals(type))) {
                    String content = m.path("content").asText("");
                    if (StringUtils.hasText(content)) {
                        answer = content;
                    }
                }
            }
        }

        if (!StringUtils.hasText(answer)) {
            log.warn("Coze message/list 未找到 assistant answer，完整响应: {}", resp.getBody());
        }
        return answer;
    }

    private boolean isCozeSuccess(JsonNode root) {
        return root.has("code") && root.get("code").asInt(-1) == 0;
    }

    private RestTemplate buildRestTemplate() {
        return new RestTemplate();
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cozeConfig.getAccessToken());
        return headers;
    }
}
