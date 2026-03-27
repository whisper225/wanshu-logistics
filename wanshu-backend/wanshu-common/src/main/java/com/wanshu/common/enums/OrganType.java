package com.wanshu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 机构类型枚举
 */
@Getter
@AllArgsConstructor
public enum OrganType {

    OLT(1, "一级转运中心"),
    TLT(2, "二级分拣中心"),
    AGENCY(3, "营业部");

    private final Integer code;
    private final String desc;

    public static OrganType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrganType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
