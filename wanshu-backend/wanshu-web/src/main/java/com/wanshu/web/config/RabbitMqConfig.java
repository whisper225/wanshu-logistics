package com.wanshu.web.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * RabbitMQ 交换机、队列与绑定配置
 *
 * <pre>
 * Exchange:  wanshu.dispatch.topic  (Topic)
 *
 * Queues:
 *   wanshu.waybill.new        — 新运单通知（快递员揽收后发）
 *   wanshu.dispatch.pending   — 拒收后重新调度
 *   wanshu.track.create       — 揽收后触发轨迹路线规划
 *   wanshu.track.complete     — 签收后标记轨迹完成
 *
 * Routing keys:
 *   waybill.new               → wanshu.waybill.new
 *   dispatch.pending          → wanshu.dispatch.pending
 *   track.create              → wanshu.track.create
 *   track.complete            → wanshu.track.complete
 * </pre>
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "wanshu.dispatch.topic";

    public static final String QUEUE_WAYBILL_NEW = "wanshu.waybill.new";
    public static final String QUEUE_DISPATCH_PENDING = "wanshu.dispatch.pending";
    public static final String QUEUE_TRACK_CREATE = "wanshu.track.create";
    public static final String QUEUE_TRACK_COMPLETE = "wanshu.track.complete";

    public static final String RK_WAYBILL_NEW = "waybill.new";
    public static final String RK_DISPATCH_PENDING = "dispatch.pending";
    public static final String RK_TRACK_CREATE = "track.create";
    public static final String RK_TRACK_COMPLETE = "track.complete";

    // ── Exchange ──────────────────────────────────────────────────

    @Bean
    public TopicExchange dispatchExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    // ── Queues ────────────────────────────────────────────────────

    @Bean
    public Queue waybillNewQueue() {
        return QueueBuilder.durable(QUEUE_WAYBILL_NEW).build();
    }

    @Bean
    public Queue dispatchPendingQueue() {
        return QueueBuilder.durable(QUEUE_DISPATCH_PENDING).build();
    }

    @Bean
    public Queue trackCreateQueue() {
        return QueueBuilder.durable(QUEUE_TRACK_CREATE).build();
    }

    @Bean
    public Queue trackCompleteQueue() {
        return QueueBuilder.durable(QUEUE_TRACK_COMPLETE).build();
    }

    // ── Bindings ──────────────────────────────────────────────────

    @Bean
    public Binding waybillNewBinding() {
        return BindingBuilder.bind(waybillNewQueue()).to(dispatchExchange()).with(RK_WAYBILL_NEW);
    }

    @Bean
    public Binding dispatchPendingBinding() {
        return BindingBuilder.bind(dispatchPendingQueue()).to(dispatchExchange()).with(RK_DISPATCH_PENDING);
    }

    @Bean
    public Binding trackCreateBinding() {
        return BindingBuilder.bind(trackCreateQueue()).to(dispatchExchange()).with(RK_TRACK_CREATE);
    }

    @Bean
    public Binding trackCompleteBinding() {
        return BindingBuilder.bind(trackCompleteQueue()).to(dispatchExchange()).with(RK_TRACK_COMPLETE);
    }

    // ── Converter & Template ──────────────────────────────────────

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        // 队列在 Broker 侧暂时消失时不抛致命异常，等待恢复后自动重声明
        factory.setMissingQueuesFatal(false);

        // 消费失败重试策略：最多 3 次，指数退避（3s → 6s → 12s）
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(3_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(30_000L);
        retryTemplate.setBackOffPolicy(backOff);
        factory.setRetryTemplate(retryTemplate);

        return factory;
    }
}
