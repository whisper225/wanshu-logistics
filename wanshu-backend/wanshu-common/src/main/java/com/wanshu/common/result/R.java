package com.wanshu.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装类
 */
@Data
public class R<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    private R() {}

    public static <T> R<T> ok() {
        return restResult(null, ResultCode.SUCCESS);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, ResultCode.SUCCESS);
    }

    public static <T> R<T> ok(T data, String message) {
        R<T> r = restResult(data, ResultCode.SUCCESS);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> fail() {
        return restResult(null, ResultCode.FAIL);
    }

    public static <T> R<T> fail(String message) {
        R<T> r = restResult(null, ResultCode.FAIL);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return restResult(null, resultCode);
    }

    public static <T> R<T> fail(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    private static <T> R<T> restResult(T data, ResultCode resultCode) {
        R<T> r = new R<>();
        r.setCode(resultCode.getCode());
        r.setMessage(resultCode.getMessage());
        r.setData(data);
        return r;
    }
}
