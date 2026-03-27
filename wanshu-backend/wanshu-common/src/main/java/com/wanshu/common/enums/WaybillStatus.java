package com.wanshu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 运单状态枚举
 */
@Getter
@AllArgsConstructor
public enum WaybillStatus {

    PENDING_DISPATCH(0, "待调度"),
    DISPATCHED(1, "已调度"),
    PENDING_PICKUP(2, "待提货"),
    IN_TRANSIT(3, "在途"),
    ARRIVED(4, "到达目的地"),
    DELIVERING(5, "派送中"),
    SIGNED(6, "已签收"),
    REJECTED(7, "已拒签"),
    EXCEPTION(8, "异常件");

    private final Integer code;
    private final String desc;

    public static WaybillStatus fromCode(Integer code) {
        for (WaybillStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的运单状态码: " + code);
    }
}
