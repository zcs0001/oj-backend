package com.tree.backend.common;

/**
 * 自定义错误码
 * 枚举类型默认是线程安全的，因为在加载枚举类型时，其实例被创建，且只创建一次，因此它天生就是单例的。
 * 泛型的好处：
 * 统一了数据类型
 * 把运行时期的问题提前到了编译期间，避免了强制类型转换可能出现的异常，因为在编译期间就能确定下来
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    /**
     * 构造函数接收状态码和消息，用于初始化枚举常量。
     * @param code
     * @param message
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
