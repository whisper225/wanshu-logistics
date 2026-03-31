package com.wanshu.web.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 消息推送服务
 *
 * <p>连接地址：ws://host:port/ws/notify/{userId}
 *
 * <p>消息类型（JSON 字符串）：
 * <ul>
 *   <li>{@code {"type":"new_pickup_task","message":"..."}}  — 新揽收任务，广播给所有在线快递员</li>
 *   <li>{@code {"type":"new_transport_task","taskId":"...","message":"..."}}  — 新运输任务，推给对应司机</li>
 *   <li>{@code {"type":"new_delivery_task","taskId":"...","message":"..."}}  — 新派件任务，推给对应快递员</li>
 * </ul>
 */
@Slf4j
@Component
@ServerEndpoint("/ws/notify/{userId}")
public class WebSocketNotifyServer {

    /** userId（字符串） → WebSocket Session */
    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    // ─────────────────────────── 生命周期 ───────────────────────────

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        SESSIONS.put(userId, session);
        log.info("[WebSocket] 连接建立，userId={}, 当前在线={}", userId, SESSIONS.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        SESSIONS.remove(userId);
        log.info("[WebSocket] 连接关闭，userId={}, 当前在线={}", userId, SESSIONS.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.debug("[WebSocket] 收到心跳/消息，userId={}: {}", userId, message);
    }

    @OnError
    public void onError(Throwable error, @PathParam("userId") String userId) {
        log.warn("[WebSocket] 连接异常，userId={}: {}", userId, error.getMessage());
        SESSIONS.remove(userId);
    }

    // ─────────────────────────── 推送 API（静态，供业务层直接调用） ───────────────────────────

    /**
     * 向指定用户推送消息。用户不在线时静默忽略。
     *
     * @param userId  系统用户 ID（Long.toString）
     * @param message JSON 格式消息体
     */
    public static void sendToUser(String userId, String message) {
        if (userId == null || message == null) return;
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("[WebSocket] 消息已推送，userId={}", userId);
            } catch (IOException e) {
                log.warn("[WebSocket] 推送失败，userId={}: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * 向所有在线用户广播消息（用于新揽收任务通知所有快递员）。
     *
     * @param message JSON 格式消息体
     */
    public static void broadcast(String message) {
        if (message == null) return;
        int sent = 0;
        for (Session session : SESSIONS.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                    sent++;
                } catch (IOException e) {
                    log.warn("[WebSocket] 广播失败，sessionId={}: {}", session.getId(), e.getMessage());
                }
            }
        }
        log.info("[WebSocket] 广播完成，消息已发送 {}/{} 个在线用户", sent, SESSIONS.size());
    }
}
