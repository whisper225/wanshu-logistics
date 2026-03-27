package com.wanshu.common.constant;

/**
 * 通用常量
 */
public class CommonConstant {

    private CommonConstant() {}

    /** 默认分页页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** Token 请求头名称 */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 验证码 Redis Key 前缀 */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";

    /** 短信验证码 Redis Key 前缀 */
    public static final String SMS_CODE_KEY_PREFIX = "sms:code:";

    /** 短信验证码过期时间（秒） */
    public static final long SMS_CODE_EXPIRE = 300;

    /** 图形验证码过期时间（秒） */
    public static final long CAPTCHA_EXPIRE = 60;

    /** 分布式锁 Key 前缀 */
    public static final String LOCK_KEY_PREFIX = "lock:";

    /** 分布式锁默认过期时间（秒） */
    public static final long LOCK_EXPIRE = 30;
}
