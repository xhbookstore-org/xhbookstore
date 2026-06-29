package com.xhbookstore.api.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * API 统一错误码管理
 * 格式：模块前缀(2位) + 错误序号(3位)
 */
public class ApiErrorCode {

    // ========== 通用错误 00xxx ==========
    public static final int SUCCESS = 0;
    public static final int UNKNOWN_ERROR = 1;
    public static final int PARAM_INVALID = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int INTERNAL_ERROR = 500;

    // ========== 认证模块 01xxx ==========
    public static final int AUTH_CODE_INVALID = 10001;
    public static final int AUTH_PHONE_EMPTY = 10002;
    public static final int AUTH_TOKEN_EXPIRED = 10003;
    public static final int AUTH_TOKEN_INVALID = 10004;
    public static final int AUTH_LOGIN_FAILED = 10005;

    // ========== 会员模块 02xxx ==========
    public static final int MEMBER_NOT_FOUND = 20001;
    public static final int MEMBER_CARD_EXPIRED = 20002;
    public static final int MEMBER_CARD_DISABLED = 20003;
    public static final int MEMBER_NO_CARD = 20004;
    public static final int MEMBER_CODE_EXPIRED = 20005;
    public static final int MEMBER_CODE_INVALID = 20006;

    // ========== 积分模块 03xxx ==========
    public static final int POINTS_NOT_ENOUGH = 30001;
    public static final int POINTS_EXCEED_MAX = 30002;
    public static final int POINTS_REASON_INVALID = 30003;
    public static final int POINTS_OPERATION_DENIED = 30004;

    // ========== 借阅模块 04xxx ==========
    public static final int BORROW_DENIED = 40001;
    public static final int BORROW_BOOK_REQUIRED = 40002;
    public static final int BORROW_IMAGE_REQUIRED = 40003;
    public static final int BORROW_RETURN_DENIED = 40004;
    public static final int BORROW_ALREADY_RETURNED = 40005;

    // ========== 文件模块 05xxx ==========
    public static final int FILE_UPLOAD_FAILED = 50001;
    public static final int FILE_SIZE_EXCEEDED = 50002;
    public static final int FILE_TYPE_INVALID = 50003;

    // ========== 账号模块 06xxx ==========
    public static final int ACCOUNT_CANNOT_CANCEL = 60001;
    public static final int ACCOUNT_HAS_UNRETURNED = 60002;
    public static final int ACCOUNT_STAFF_ACTIVE = 60003;

    private static final Map<Integer, String> MESSAGES = new HashMap<>();

    static {
        MESSAGES.put(SUCCESS, "操作成功");
        MESSAGES.put(UNKNOWN_ERROR, "未知错误");
        MESSAGES.put(PARAM_INVALID, "参数无效");
        MESSAGES.put(UNAUTHORIZED, "未登录或登录已过期");
        MESSAGES.put(FORBIDDEN, "无权限访问");
        MESSAGES.put(NOT_FOUND, "资源不存在");
        MESSAGES.put(METHOD_NOT_ALLOWED, "请求方法不允许");
        MESSAGES.put(INTERNAL_ERROR, "服务器内部错误");

        MESSAGES.put(AUTH_CODE_INVALID, "微信授权码无效");
        MESSAGES.put(AUTH_PHONE_EMPTY, "未获取到手机号");
        MESSAGES.put(AUTH_TOKEN_EXPIRED, "登录已过期，请重新登录");
        MESSAGES.put(AUTH_TOKEN_INVALID, "无效的访问令牌");
        MESSAGES.put(AUTH_LOGIN_FAILED, "登录失败");

        MESSAGES.put(MEMBER_NOT_FOUND, "会员不存在");
        MESSAGES.put(MEMBER_CARD_EXPIRED, "会员卡已过期");
        MESSAGES.put(MEMBER_CARD_DISABLED, "会员卡不可用");
        MESSAGES.put(MEMBER_NO_CARD, "未办理会员卡");
        MESSAGES.put(MEMBER_CODE_EXPIRED, "会员码已过期");
        MESSAGES.put(MEMBER_CODE_INVALID, "无效的会员码");

        MESSAGES.put(POINTS_NOT_ENOUGH, "积分不足");
        MESSAGES.put(POINTS_EXCEED_MAX, "超过单次最大积分限制");
        MESSAGES.put(POINTS_REASON_INVALID, "无效的积分事项");
        MESSAGES.put(POINTS_OPERATION_DENIED, "无积分操作权限");

        MESSAGES.put(BORROW_DENIED, "不允许办理借阅");
        MESSAGES.put(BORROW_BOOK_REQUIRED, "至少需要一本图书");
        MESSAGES.put(BORROW_IMAGE_REQUIRED, "每本书至少需要一张图片");
        MESSAGES.put(BORROW_RETURN_DENIED, "不允许办理还书");
        MESSAGES.put(BORROW_ALREADY_RETURNED, "该图书已还，不可重复操作");

        MESSAGES.put(FILE_UPLOAD_FAILED, "文件上传失败");
        MESSAGES.put(FILE_SIZE_EXCEEDED, "文件大小超出限制");
        MESSAGES.put(FILE_TYPE_INVALID, "不支持的文件类型");

        MESSAGES.put(ACCOUNT_CANNOT_CANCEL, "当前账号不允许注销");
        MESSAGES.put(ACCOUNT_HAS_UNRETURNED, "存在未还图书，无法注销");
        MESSAGES.put(ACCOUNT_STAFF_ACTIVE, "员工身份仍有效，无法注销");
    }

    public static String getMessage(int code) {
        return MESSAGES.getOrDefault(code, "未知错误");
    }

    public static String getMessage(int code, String defaultMsg) {
        return MESSAGES.getOrDefault(code, defaultMsg);
    }
}
