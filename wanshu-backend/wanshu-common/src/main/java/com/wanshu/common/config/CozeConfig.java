package com.wanshu.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Coze AI 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "coze")
public class CozeConfig {

    /** Coze API Base URL */
    private String apiBaseUrl = "https://api.coze.cn";

    /** Personal Access Token */
    private String accessToken;

    /** 智能客服 Bot ID */
    private String customerBotId;

    /** 调度辅助 Bot ID */
    private String dispatchBotId;

    /** 路径规划工作流 ID（轨迹生成：高德路线 → polyline） */
    private String routeWorkflowId;

    /** 请求超时时间（秒） */
    private Integer timeout = 60;
}
