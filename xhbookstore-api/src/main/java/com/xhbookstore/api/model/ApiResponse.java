package com.xhbookstore.api.model;

import com.xhbookstore.api.constant.ApiErrorCode;

/**
 * 小程序API统一响应结构
 * { code: 0, message: "操作成功", data: {...}, requestId: "xxx" }
 */
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private String requestId;

    public ApiResponse() {}

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiErrorCode.SUCCESS, ApiErrorCode.getMessage(ApiErrorCode.SUCCESS), data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ApiErrorCode.SUCCESS, message, data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ApiErrorCode.SUCCESS, ApiErrorCode.getMessage(ApiErrorCode.SUCCESS), null);
    }

    public static <T> ApiResponse<T> error(int code) {
        return new ApiResponse<>(code, ApiErrorCode.getMessage(code), null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
