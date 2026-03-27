package com.wanshu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发车周期枚举
 */
@Getter
@AllArgsConstructor
public enum PeriodType {

    DAILY(1, "天"),
    WEEKLY(2, "周"),
    MONTHLY(3, "月"),
    ONCE(4, "一次");

    private final Integer code;
    private final String desc;

    public static PeriodType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PeriodType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
