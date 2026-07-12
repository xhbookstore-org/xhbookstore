package com.xhbookstore.api.controller;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.service.ICosService;
import com.xhbookstore.api.service.ImageUploadValidator;
import com.xhbookstore.api.service.ImageUploadValidator.ValidatedImage;
import com.xhbookstore.system.domain.book.BookBorrowDetail;
import com.xhbookstore.system.domain.book.BookBorrowDetailImage;
import com.xhbookstore.system.domain.book.BookBorrowOrder;
import com.xhbookstore.system.domain.book.BookImage;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.mapper.book.BookBorrowDetailImageMapper;
import com.xhbookstore.system.mapper.book.BookBorrowDetailMapper;
import com.xhbookstore.system.mapper.book.BookBorrowOrderMapper;
import com.xhbookstore.system.mapper.book.BookImageMapper;
import com.xhbookstore.system.mapper.book.BookInfoMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "File upload", description = "Secure image upload to Tencent COS")
@RestController
@RequestMapping("/api/mp/v1/files")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private static final int MAX_IMAGES_PER_RESOURCE_TYPE = 10;

    @Autowired private ICosService cosService;
    @Autowired private ImageUploadValidator imageValidator;
    @Autowired private BookImageMapper bookImageMapper;
    @Autowired private BookBorrowDetailImageMapper borrowDetailImageMapper;
    @Autowired private BookBorrowDetailMapper borrowDetailMapper;
    @Autowired private BookBorrowOrderMapper borrowOrderMapper;
    @Autowired private BookInfoMapper bookInfoMapper;
    @Autowired private MemberMapper memberMapper;

    @Operation(summary = "Upload book attachment image")
    @PostMapping("/book-attachment-images")
    public ApiResponse<Map<String, Object>> uploadBookImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String memberId,
            @RequestParam(required = false) String borrowOrderNo,
            @RequestParam(required = false) Long borrowDetailId,
            @RequestParam(required = false) Long borrowOrderId,
            @RequestParam(defaultValue = "1") Integer imageType,
            @RequestParam(required = false) Long bookId,
            HttpServletRequest request) {

        if (imageType == null || imageType < 1 || imageType > 3) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "Invalid image type");
        }
        boolean staff = Boolean.TRUE.equals(request.getAttribute("isStaff"));
        Integer tokenMemberId = integerAttr(request, "memberId");
        Long staffUserId = longAttr(request, "staffUserId");
        UploadResource resource = resolveResource(memberId, borrowOrderNo, borrowDetailId,
                borrowOrderId, bookId, imageType, staff, tokenMemberId);
        ValidatedImage validated = imageValidator.validate(file);

        String actorId = staff ? String.valueOf(staffUserId) : "member:" + tokenMemberId;
        String actorName = staff ? "staff" : "member";
        String folder = resource.bookId() != null && resource.borrowDetail() == null
                ? "bookstore/books/" + resource.bookId()
                : "bookstore/" + resource.memberId();
        Map<String, String> uploadResult = null;

        try {
            uploadResult = cosService.upload(new ByteArrayInputStream(validated.bytes()),
                    validated.safeFileName(), validated.contentType(), folder);
            saveResourceImage(resource, uploadResult, imageType, actorId, actorName, validated.safeFileName());

            Map<String, Object> data = new HashMap<>();
            data.put("imageId", uploadResult.get("imageId"));
            data.put("url", uploadResult.get("url"));
            data.put("thumbUrl", uploadResult.get("thumbUrl"));
            data.put("size", validated.bytes().length);
            data.put("fileName", validated.safeFileName());
            data.put("format", validated.extension());
            data.put("width", validated.width());
            data.put("height", validated.height());
            log.info("[image upload success] actor={}, memberId={}, detailId={}, bookId={}, key={}, size={}",
                    actorId, resource.memberId(), borrowDetailId, resource.bookId(), uploadResult.get("key"),
                    validated.bytes().length);
            return ApiResponse.success(data);
        } catch (ApiException e) {
            deleteUploadedObject(uploadResult);
            throw e;
        } catch (Exception e) {
            deleteUploadedObject(uploadResult);
            log.warn("[image upload failed] actor={}, memberId={}, detailId={}, bookId={}, reason={}",
                    actorId, resource.memberId(), borrowDetailId, resource.bookId(), e.getMessage());
            throw new ApiException(ApiErrorCode.FILE_UPLOAD_FAILED, "Image upload failed");
        }
    }

    private UploadResource resolveResource(String requestedMemberId, String requestedOrderNo,
                                            Long detailId, Long requestedOrderId, Long requestedBookId,
                                            Integer imageType, boolean staff, Integer tokenMemberId) {
        if (detailId != null && requestedBookId != null) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "Borrow detail and book cover cannot be mixed");
        }
        if (requestedBookId != null) {
            if (!staff) throw new ApiException(ApiErrorCode.FORBIDDEN, "Only staff can upload book covers");
            if (bookInfoMapper.selectById(requestedBookId) == null) {
                throw new ApiException(ApiErrorCode.NOT_FOUND, "Book not found");
            }
            if (bookImageMapper.countByBookAndType(requestedBookId, imageType) >= MAX_IMAGES_PER_RESOURCE_TYPE) {
                throw new ApiException(ApiErrorCode.PARAM_INVALID, "Image limit exceeded");
            }
            return new UploadResource(null, requestedBookId, null, null);
        }
        if (detailId != null) {
            BookBorrowDetail detail = borrowDetailMapper.selectById(detailId);
            if (detail == null) throw new ApiException(ApiErrorCode.NOT_FOUND, "Borrow detail not found");
            BookBorrowOrder order = borrowOrderMapper.selectById(detail.getBorrowOrderId());
            if (order == null || !order.getOrderNo().equals(detail.getBorrowOrderNo())) {
                throw new ApiException(ApiErrorCode.PARAM_INVALID, "Borrow detail relationship is invalid");
            }
            assertMatches(requestedMemberId, order.getMemberId(), "memberId");
            assertMatches(requestedOrderId, order.getId(), "borrowOrderId");
            assertMatches(requestedOrderNo, order.getOrderNo(), "borrowOrderNo");
            if (!staff && !order.getMemberId().equals(tokenMemberId)) {
                throw new ApiException(ApiErrorCode.FORBIDDEN, "No permission for this borrow detail");
            }
            if (borrowDetailImageMapper.countByDetailAndType(detailId, imageType) >= MAX_IMAGES_PER_RESOURCE_TYPE) {
                throw new ApiException(ApiErrorCode.PARAM_INVALID, "Image limit exceeded");
            }
            return new UploadResource(order.getMemberId(), detail.getBookId(), detail, order);
        }

        Integer resolvedMemberId = parseInteger(requestedMemberId);
        if (resolvedMemberId == null) throw new ApiException(ApiErrorCode.PARAM_INVALID, "memberId is required");
        Member member = memberMapper.selectMemberById(resolvedMemberId);
        if (member == null || member.getStatus() == null || member.getStatus() != 0) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        }
        if (!staff && !resolvedMemberId.equals(tokenMemberId)) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "No permission for this member");
        }
        return new UploadResource(resolvedMemberId, null, null, null);
    }

    private void saveResourceImage(UploadResource resource, Map<String, String> result, Integer imageType,
                                   String actorId, String actorName, String imageName) {
        if (resource.borrowDetail() != null) {
            BookBorrowDetailImage image = new BookBorrowDetailImage();
            image.setBorrowDetailId(resource.borrowDetail().getId());
            image.setBorrowOrderId(resource.borrowOrder().getId());
            image.setBorrowOrderNo(resource.borrowOrder().getOrderNo());
            image.setImageId(result.get("imageId"));
            image.setImageName(imageName);
            image.setImageUrl(result.get("url"));
            image.setThumbUrl(result.get("thumbUrl"));
            image.setImageType(imageType);
            image.setCreateStaffId(actorId);
            image.setCreateStaffName(actorName);
            if (borrowDetailImageMapper.insert(image) != 1) throw new IllegalStateException("Image record insert failed");
        } else if (resource.bookId() != null) {
            BookImage image = new BookImage();
            image.setBookId(resource.bookId());
            image.setImageId(result.get("imageId"));
            image.setImageName(imageName);
            image.setImageUrl(result.get("url"));
            image.setThumbUrl(result.get("thumbUrl"));
            image.setImageType(imageType);
            image.setCreateStaffId(actorId);
            image.setCreateStaffName(actorName);
            if (bookImageMapper.insert(image) != 1) throw new IllegalStateException("Image record insert failed");
        }
    }

    private void deleteUploadedObject(Map<String, String> result) {
        if (result == null) return;
        try {
            cosService.delete(result.get("key"));
        } catch (Exception cleanupError) {
            log.error("[image cleanup failed] key={}", result.get("key"), cleanupError);
        }
    }

    private void assertMatches(Object requested, Object actual, String field) {
        if (requested != null && !requested.toString().isBlank()
                && (actual == null || !requested.toString().equals(actual.toString()))) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, field + " does not match borrow detail");
        }
    }

    private Integer parseInteger(Object value) {
        try {
            return value == null || value.toString().isBlank() ? null : Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "Invalid memberId");
        }
    }

    private Integer integerAttr(HttpServletRequest request, String name) {
        return parseInteger(request.getAttribute(name));
    }

    private Long longAttr(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value == null ? null : Long.valueOf(value.toString());
    }

    private record UploadResource(Integer memberId, Long bookId, BookBorrowDetail borrowDetail,
                                  BookBorrowOrder borrowOrder) {
    }
}
