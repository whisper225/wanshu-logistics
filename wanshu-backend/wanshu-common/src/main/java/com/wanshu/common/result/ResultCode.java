package com.wanshu.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 认证相关 4xx
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // 参数校验
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(400, "缺少必填参数"),

    // 业务错误
    DATA_EXIST(5001, "数据已存在"),
    DATA_NOT_EXIST(5002, "数据不存在"),
    ORDER_STATUS_ERROR(5003, "订单状态异常"),
    WAYBILL_STATUS_ERROR(5004, "运单状态异常"),

    // 第三方服务
    OSS_UPLOAD_ERROR(6001, "文件上传失败"),
    COZE_API_ERROR(6002, "AI 服务调用失败"),
    SMS_SEND_ERROR(6003, "短信发送失败");

    private final Integer code;
    private final String message;
}
