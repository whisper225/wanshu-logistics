package com.wanshu.base.freight;

/**
 * 普快默认轻抛系数（cm³/kg），模板未填 lightThrowRatio 时按类型回落。
 */
public final class LightThrowDefaults {

    /** 同城互寄 */
    public static final int SAME_CITY = 12000;
    /** 省内寄件 */
    public static final int SAME_PROVINCE = 8000;
    /** 跨省寄件 */
    public static final int CROSS_PROVINCE = 12000;
    /** 经济区互寄：未绑定经济区时的默认 */
    public static final int ECONOMIC_ZONE_FALLBACK = 6000;

    private LightThrowDefaults() {
    }

    public static int byTemplateType(int templateType) {
        return switch (templateType) {
            case 1 -> SAME_CITY;
            case 2 -> SAME_PROVINCE;
            case 3 -> CROSS_PROVINCE;
            case 4 -> ECONOMIC_ZONE_FALLBACK;
            default -> ECONOMIC_ZONE_FALLBACK;
        };
    }
}
