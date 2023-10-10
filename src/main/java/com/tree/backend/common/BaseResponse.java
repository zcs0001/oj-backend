package com.tree.backend.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 通用返回类
 * BaseResponse类负责定义通用返回结构，而ResultUtils类负责提供创建返回对象的工具方法。
 * 泛型类： BaseResponse 是一个泛型类，可以在创建对象时指定具体的类型
 * 例如 BaseResponse<String> 或 BaseResponse<Integer>。
 * Serializable 接口：表示该类的对象可以被序列化，可以在网络上传输或保存到文件中。
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    /**
     * 构造函数声明：通过构造函数，可以在创建对象的同时为对象的属性提供初始值，确保对象被创建时就有合适的状态。
     *            即在构造的过程中已经new了一个对象
     * 有参构造函数：接收状态码、数据和消息，用于创建包含详细信息的返回对象。
     * 有参构造函数：接收状态码和数据，消息为空。
     * 有参构造函数：接收ErrorCode对象，用该对象的状态码和消息创建返回对象。
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
