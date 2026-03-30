package com.wanshu.web.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.R;
import com.wanshu.web.service.app.AppAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户端 AI 智能客服接口
 * <p>
 * Coze 工作流说明：
 *   智能客服 Bot 内置以下处理节点：
 *   1. 意图识别节点  — 判断用户问题类型（物流追踪/寄件流程/价格咨询/其他）
 *   2. 知识库检索节点 — 从万枢物流知识库（FAQ、运费规则、服务范围）中召回相关片段
 *   3. 大模型生成节点 — 结合检索结果生成准确、友好的回复
 *   4. 兜底节点      — 无法解答时引导用户联系人工客服
 *
 * 两种调用模式：
 *   - POST /api/app/ai/chat        非流式，适合简短问答
 *   - GET  /api/app/ai/chat/stream SSE 流式，适合长文回复（打字机效果）
 */
@Tag(name = "用户端（小程序）- AI 客服")
@RestController
@RequestMapping("/api/app/ai")
@RequiredArgsConstructor
public class AppAiController {

    private final AppAiService appAiService;

    private static final ExecutorService SSE_EXECUTOR = Executors.newCachedThreadPool();

    /**
     * 非流式对话：等待 Bot 完整回复后一次性返回。
     *
     * 请求体示例：
     * <pre>{"query": "寄件要多少钱？"}</pre>
     */
    @Operation(summary = "AI 客服对话（非流式）")
    @PostMapping("/chat")
    public R<String> chat(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        if (!StringUtils.hasText(query)) {
            throw new BusinessException("问题内容不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        String reply = appAiService.chat(userId, query);
        return R.ok(reply);
    }

    /**
     * SSE 流式对话：Bot 逐段推送回复，前端实时展示（打字机效果）。
     *
     * 请求示例：GET /api/app/ai/chat/stream?query=我的包裹在哪里
     *
     * 前端使用 EventSource 或 uni.request 监听 text/event-stream。
     * 每个 data 事件携带一段文本片段，连接关闭表示回复完毕。
     */
    @Operation(summary = "AI 客服对话（SSE 流式）")
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String query) {
        if (!StringUtils.hasText(query)) {
            throw new BusinessException("问题内容不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        SseEmitter emitter = new SseEmitter(60_000L);
        SSE_EXECUTOR.submit(() -> appAiService.chatStream(userId, query, emitter));
        return emitter;
    }
}
