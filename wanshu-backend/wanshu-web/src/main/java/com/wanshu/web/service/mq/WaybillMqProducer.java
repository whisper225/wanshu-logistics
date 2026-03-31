package com.wanshu.web.service.mq;

import com.wanshu.web.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * 运单消息发布：新运单通知 / 重新调度通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaybillMqProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 快递员揽收完成后，发送新运单消息到调度中心。
     *
     * @param waybillId 运单 ID
     */
    public void sendNewWaybill(Long waybillId) {
        log.info("[MQ] 发送新运单消息，waybillId={}", waybillId);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.RK_WAYBILL_NEW,
                waybillId);
    }

    /**
     * 派件被拒收后，重新触发调度（为该运单重新分配快递员）。
     *
     * @param waybillId 运单 ID
     */
    public void sendPendingDispatch(Long waybillId) {
        log.info("[MQ] 发送重新调度消息，waybillId={}", waybillId);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.RK_DISPATCH_PENDING,
                waybillId);
    }

    /**
     * 快递员揽收完成后，发送轨迹创建消息，触发 MQ 监听器异步规划路线并存入 MongoDB。
     *
     * @param waybillId 运单 ID
     */
    public void sendTrackCreate(Long waybillId) {
        log.info("[MQ] 发送轨迹创建消息，waybillId={}", waybillId);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.RK_TRACK_CREATE,
                waybillId);
    }

    /**
     * 签收完成后，发送轨迹完成消息，标记该运单轨迹路线为已完成状态，不再更新位置。
     *
     * @param waybillId 运单 ID
     */
    public void sendTrackComplete(Long waybillId) {
        log.info("[MQ] 发送轨迹完成消息，waybillId={}", waybillId);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.RK_TRACK_COMPLETE,
                waybillId);
    }
}
