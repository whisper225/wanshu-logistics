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
 * 支持普通请求和 SSE 流式请求两种模式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CozeUtil {

    private final CozeConfig cozeConfig;
    private final ObjectMapper objectMapper;

    /**
     * 普通对话（非流式），等待完整回复后返回
     *
     * @param botId  Bot ID
     * @param userId 用户标识
     * @param query  用户提问内容
     * @return AI 回复文本
     */
    public String chat(String botId, String userId, String query) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cozeConfig.getAccessToken());

            Map<String, Object> body = new HashMap<>();
            body.put("bot_id", botId);
            body.put("user_id", userId);
            body.put("stream", false);
            body.put("auto_save_history", true);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", query);
            message.put("content_type", "text");
            body.put("additional_messages", new Object[]{message});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            String apiUrl = cozeConfig.getApiBaseUrl() + "/v3/chat";
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("code") && root.get("code").asInt() == 0) {
                return root.path("data").path("content").asText("");
            }
            log.warn("Coze API 返回异常: {}", response.getBody());
            throw new BusinessException(ResultCode.COZE_API_ERROR);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Coze API 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.COZE_API_ERROR);
        }
    }

    /**
     * 流式对话（SSE），逐步推送到前端
     *
     * @param botId   Bot ID
     * @param userId  用户标识
     * @param query   用户提问内容
     * @param emitter SSE 发射器
     */
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

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", query);
            message.put("content_type", "text");
            body.put("additional_messages", new Object[]{message});

            String jsonBody = objectMapper.writeValueAsString(body);
            connection.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) {
                            emitter.complete();
                            return;
                        }
                        try {
                            JsonNode event = objectMapper.readTree(data);
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
                        } catch (Exception e) {
                            log.debug("解析 SSE 事件跳过: {}", data);
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

    /**
     * 智能客服对话（非流式）
     */
    public String customerChat(String userId, String query) {
        return chat(cozeConfig.getCustomerBotId(), userId, query);
    }

    /**
     * 智能客服对话（流式）
     */
    public void customerChatStream(String userId, String query, SseEmitter emitter) {
        chatStream(cozeConfig.getCustomerBotId(), userId, query, emitter);
    }

    /**
     * 调度辅助对话（非流式）
     */
    public String dispatchChat(String userId, String query) {
        return chat(cozeConfig.getDispatchBotId(), userId, query);
    }

    /**
     * 调度辅助对话（流式）
     */
    public void dispatchChatStream(String userId, String query, SseEmitter emitter) {
        chatStream(cozeConfig.getDispatchBotId(), userId, query, emitter);
    }
}
