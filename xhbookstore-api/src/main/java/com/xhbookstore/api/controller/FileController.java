package com.xhbookstore.api.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;

/**
 * 文件上传接口 - 文档 §10
 */
@RestController
@RequestMapping("/api/mp/v1/files")
public class FileController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L; // 10MB

    /**
     * 上传图书附件图片 §10.1
     */
    @PostMapping("/book-attachment-images")
    public ApiResponse<Map<String, Object>> uploadBookImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("memberId") String memberId) {

        if (file.isEmpty()) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException(ApiErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new ApiException(ApiErrorCode.FILE_TYPE_INVALID);
        }

        // TODO: 实际文件存储（OSS/本地）
        String imageId = UUID.randomUUID().toString();
        String url = "/uploads/" + imageId + "_" + file.getOriginalFilename();

        Map<String, Object> data = new HashMap<>();
        data.put("imageId", imageId);
        data.put("url", url);
        data.put("thumbUrl", url + "?thumb=1");
        data.put("size", file.getSize());
        return ApiResponse.success(data);
    }
}
