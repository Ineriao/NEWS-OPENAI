package com.news.common;

/**
 * 自定义业务异常
 * 用于在业务逻辑中抛出可预期的异常
 */
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    // ==================== 常用异常工厂方法 ====================

    /** 参数错误 */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    /** 未授权 */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    /** 无权限 */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /** 资源不存在 */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }
}
