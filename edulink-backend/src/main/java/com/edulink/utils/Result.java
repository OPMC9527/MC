package com.edulink.utils;

import lombok.Data;

/**
 * 统一响应结果封装类
 * 用于规范前后端交互的API响应格式，包含状态码、消息、数据体和时间戳
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {

    /**
     * 响应状态码（200表示成功，其他表示错误）
     */
    private Integer code;

    /**
     * 响应消息（成功时为"success"，错误时为具体错误描述）
     */
    private String message;

    /**
     * 响应数据（成功时携带的数据对象）
     */
    private T data;

    /**
     * 响应时间戳（毫秒，从1970-01-01开始）
     */
    private Long timestamp;

    /**
     * 无参构造器，自动生成当前时间戳
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 返回成功响应（仅携带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 返回成功响应（自定义消息并携带数据）
     *
     * @param message 成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 返回错误响应（默认错误码500）
     *
     * @param message 错误描述
     * @param <T>     数据类型（通常为Void）
     * @return 错误响应对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 返回错误响应（自定义错误码）
     *
     * @param code    错误码（如400、401、403、409等）
     * @param message 错误描述
     * @param <T>     数据类型（通常为Void）
     * @return 错误响应对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}