package com.wanshu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    CREATED(0, "已下单"),
    ACCEPTED(1, "已接单"),
    PICKED_UP(2, "已揽收"),
    IN_TRANSIT(3, "运输中"),
    DELIVERING(4, "派送中"),
    SIGNED(5, "已签收"),
    REJECTED(6, "已拒收"),
    CANCELLED(7, "已取消");

    private final Integer code;
    private final String desc;

    public static OrderStatus fromCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态码: " + code);
    }
}
