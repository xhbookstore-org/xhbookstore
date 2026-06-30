package com.xhbookstore.api.controller;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.service.ICosService;
import com.xhbookstore.system.domain.book.BookImage;
import com.xhbookstore.system.domain.book.BookBorrowDetailImage;
import com.xhbookstore.system.mapper.book.BookImageMapper;
import com.xhbookstore.system.mapper.book.BookBorrowDetailImageMapper;

/**
 * 文件上传接口 - 腾讯云COS
 */
@RestController
@RequestMapping("/api/mp/v1/files")
public class FileController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L; // 10MB

    @Autowired
    private ICosService cosService;
    @Autowired
    private BookImageMapper bookImageMapper;
    @Autowired
    private BookBorrowDetailImageMapper borrowDetailImageMapper;

    /**
     * 上传图书附件图片 §10.1 — 用于借书/还书时拍摄
     * @param file         图片文件
     * @param memberId     会员ID
     * @param borrowOrderNo 借书单号（可选，关联借书单时传入）
     * @param borrowDetailId 借书明细ID（可选）
     * @param imageType    图片类型：1-借书拍摄 2-还书拍摄 3-损坏记录
     * @param bookId       图书ID（可选，上传图书封面时传入）
     */
    @PostMapping("/book-attachment-images")
    public ApiResponse<Map<String, Object>> uploadBookImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("memberId") String memberId,
            @RequestParam(required = false) String borrowOrderNo,
            @RequestParam(required = false) Long borrowDetailId,
            @RequestParam(required = false) Long borrowOrderId,
            @RequestParam(defaultValue = "1") Integer imageType,
            @RequestParam(required = false) Long bookId,
            HttpServletRequest request) {

        // 校验
        if (file.isEmpty()) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException(ApiErrorCode.FILE_SIZE_EXCEEDED);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException(ApiErrorCode.FILE_TYPE_INVALID);
        }

        String staffId = (String) request.getAttribute("userId");
        if (staffId == null) staffId = "system";
        String staffName = "用户";

        try {
            // 上传到腾讯云COS
            String folder = "bookstore/" + memberId;
            Map<String, String> uploadResult = cosService.upload(
                    file.getInputStream(), file.getOriginalFilename(), contentType, folder);

            String imageId = uploadResult.get("imageId");
            String url = uploadResult.get("url");
            String thumbUrl = uploadResult.get("thumbUrl");

            // 根据场景写入不同的图片表
            if (borrowDetailId != null && borrowOrderNo != null) {
                // 借书/还书明细图片
                BookBorrowDetailImage img = new BookBorrowDetailImage();
                img.setBorrowDetailId(borrowDetailId);
                img.setBorrowOrderId(borrowOrderId != null ? borrowOrderId : 0L);
                img.setBorrowOrderNo(borrowOrderNo);
                img.setImageId(imageId);
                img.setImageName(file.getOriginalFilename());
                img.setImageUrl(url);
                img.setThumbUrl(thumbUrl);
                img.setImageType(imageType);
                img.setCreateStaffId(staffId);
                img.setCreateStaffName(staffName);
                borrowDetailImageMapper.insert(img);
            } else if (bookId != null) {
                // 图书图片
                BookImage img = new BookImage();
                img.setBookId(bookId);
                img.setImageId(imageId);
                img.setImageName(file.getOriginalFilename());
                img.setImageUrl(url);
                img.setThumbUrl(thumbUrl);
                img.setImageType(imageType);
                img.setCreateStaffId(staffId);
                img.setCreateStaffName(staffName);
                bookImageMapper.insert(img);
            }
            // 否则仅上传不记录（由业务方后续调用关联）

            Map<String, Object> data = new HashMap<>();
            data.put("imageId", imageId);
            data.put("url", url);
            data.put("thumbUrl", thumbUrl);
            data.put("size", file.getSize());
            data.put("fileName", file.getOriginalFilename());
            return ApiResponse.success(data);

        } catch (Exception e) {
            throw new ApiException(ApiErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }
}
