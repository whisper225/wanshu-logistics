package com.wanshu.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

/**
 * 统一解决前端 JavaScript Number 精度问题：雪花 ID 等超出 IEEE754 安全整数范围的 Long 以 JSON 字符串输出；
 * 较小的 Long（分页 total、耗时 ms、6 位 adcode 等）仍为数字，避免破坏现有前端逻辑。
 *
 * <p>新模块实体不必再逐个给 {@code id}、{@code parentId} 等加 {@code @JsonSerialize}。</p>
 */
@Configuration
public class JacksonConfig {

    /** JS Number.MAX_SAFE_INTEGER */
    private static final long MAX_SAFE = 9007199254740991L;

    /**
     * 在 Spring Boot 默认 Jackson 配置之后追加模块，避免被覆盖（参见 Spring Boot #39592 讨论）。
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public Jackson2ObjectMapperBuilderCustomizer longPrecisionSafeJson() {
        return builder -> {
            JsonSerializer<Long> serializer = new JsonSerializer<>() {
                @Override
                public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (value == null) {
                        gen.writeNull();
                        return;
                    }
                    if (value > MAX_SAFE || value < -MAX_SAFE) {
                        gen.writeString(Long.toString(value));
                    } else {
                        gen.writeNumber(value);
                    }
                }
            };
            SimpleModule longModule = new SimpleModule("wanshu-long-precision");
            longModule.addSerializer(Long.class, serializer);
            longModule.addSerializer(Long.TYPE, serializer);
            // 与 Long 自定义模块一并注册，避免仅追加自定义 Module 时部分环境下丢失 JSR-310（LocalDateTime 等）
            builder.modules(new JavaTimeModule(), longModule);
        };
    }
}
