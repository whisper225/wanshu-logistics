package com.wanshu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作状态枚举
 */
@Getter
@AllArgsConstructor
public enum WorkStatus {

    REST(0, "休息"),
    WORKING(1, "上班");

    private final Integer code;
    private final String desc;

    public static WorkStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WorkStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
