package com.wanshu.web.service.app;

import com.wanshu.common.utils.CozeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 用户端 AI 智能客服服务
 * <p>
 * 基于 Coze 平台的客服 Bot，支持非流式（同步返回）和 SSE 流式两种模式。
 * Coze Bot 工作流说明：
 *   - 接收用户问题（物流查询、寄件疑问、价格咨询等）
 *   - Bot 内部通过知识库检索 + 大模型生成回答
 *   - 多轮对话通过 userId 自动关联历史
 */
@Service
@RequiredArgsConstructor
public class AppAiService {

    private final CozeUtil cozeUtil;

    /**
     * 非流式对话：等待 Coze Bot 完整回复后返回文本。
     * 适用于简短问答场景，前端直接展示结果。
     *
     * @param userId 当前登录用户 ID（字符串），作为 Coze 会话标识以保留多轮上下文
     * @param query  用户输入的问题
     * @return AI 回复文本
     */
    public String chat(Long userId, String query) {
        return cozeUtil.customerChat(userId.toString(), query);
    }

    /**
     * SSE 流式对话：Coze Bot 逐段推送回复内容到前端。
     * 适用于长文回复场景，提升用户体验（打字机效果）。
     *
     * @param userId  当前登录用户 ID（字符串）
     * @param query   用户输入的问题
     * @param emitter SSE 发射器，由控制器创建并传入
     */
    public void chatStream(Long userId, String query, SseEmitter emitter) {
        cozeUtil.customerChatStream(userId.toString(), query, emitter);
    }
}
