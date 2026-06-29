package com.xhbookstore.api.exception;

/**
 * API业务异常
 */
public class ApiException extends RuntimeException {

    private int code;

    public ApiException(int code) {
        super(com.xhbookstore.api.constant.ApiErrorCode.getMessage(code));
        this.code = code;
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}
