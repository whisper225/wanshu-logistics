package com.wanshu.system.config;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码编码器 — 基于 Hutool BCrypt 实现
 * 替代 Spring Security 的 BCryptPasswordEncoder，避免引入整个 spring-security 依赖
 */
public class PasswordEncoder {

    /**
     * 加密明文密码
     */
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码与密文是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}

