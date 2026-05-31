package com.edulink.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于在业务逻辑层抛出可预知的业务异常（如参数校验失败、数据不存在、权限不足等）
 * 与系统级异常（如空指针、数据库连接失败）区分开，便于统一处理和向前端返回友好提示
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码，默认为500（服务器内部错误）
     * 可根据业务需要自定义，如400表示参数错误，403表示无权限等
     */
    private final Integer code;

    /**
     * 构造业务异常（使用默认错误码500）
     *
     * @param message 异常信息（将返回给前端）
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造业务异常（自定义错误码）
     *
     * @param code    错误码
     * @param message 异常信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}