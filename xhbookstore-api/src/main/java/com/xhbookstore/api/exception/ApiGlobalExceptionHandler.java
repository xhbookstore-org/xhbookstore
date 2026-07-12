package com.xhbookstore.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.system.service.book.BookBorrowException;

/**
 * 全局异常处理
 */
@RestControllerAdvice("com.xhbookstore.api.controller")
public class ApiGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiGlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ApiResponse<?> handleApiException(ApiException e, HttpServletRequest request) {
        log.warn("[API异常] path={}, code={}, message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BookBorrowException.class)
    public ApiResponse<?> handleBookBorrowException(BookBorrowException e, HttpServletRequest request) {
        String path = request.getRequestURI();
        int code = path.contains("/borrow-returns") || path.contains("/borrow-purchases")
                ? ApiErrorCode.BORROW_RETURN_DENIED
                : ApiErrorCode.BORROW_DENIED;
        log.warn("[API business exception] path={}, code={}, message={}", path, code, e.getMessage());
        return ApiResponse.error(code, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<?> handleValidationException(Exception e, HttpServletRequest request) {
        String msg = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException ex && ex.getBindingResult().getFieldError() != null) {
            msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof BindException ex && ex.getFieldError() != null) {
            msg = ex.getFieldError().getDefaultMessage();
        }
        log.warn("[参数校验失败] path={}, message={}", request.getRequestURI(), msg);
        return ApiResponse.error(ApiErrorCode.PARAM_INVALID, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<?> handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("[缺少参数] path={}, param={}", request.getRequestURI(), e.getParameterName());
        return ApiResponse.error(ApiErrorCode.PARAM_INVALID, "缺少必要参数：" + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<?> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("[参数类型错误] path={}, param={}", request.getRequestURI(), e.getName());
        return ApiResponse.error(ApiErrorCode.PARAM_INVALID, "参数类型错误：" + e.getName());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleUnknownException(Exception e, HttpServletRequest request) {
        log.error("[系统异常] path={}", request.getRequestURI(), e);
        return ApiResponse.error(ApiErrorCode.INTERNAL_ERROR, "服务器内部错误");
    }
}
