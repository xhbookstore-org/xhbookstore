package com.xhbookstore.api.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;

@Component
public class ImageUploadValidator {
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024L;
    private static final int MAX_DIMENSION = 4096;
    private static final long MAX_PIXELS = 16_000_000L;
    private static final Set<String> JPEG_EXTENSIONS = Set.of("jpg", "jpeg");

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException(ApiErrorCode.FILE_SIZE_EXCEEDED);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ApiException(ApiErrorCode.FILE_UPLOAD_FAILED, "Unable to read image");
        }

        ImageFormat format = detectFormat(bytes);
        String originalExtension = extension(file.getOriginalFilename());
        if (!format.matchesExtension(originalExtension)) {
            throw new ApiException(ApiErrorCode.FILE_TYPE_INVALID, "Image extension does not match file content");
        }

        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new ApiException(ApiErrorCode.FILE_TYPE_INVALID, "Invalid image data");
        }
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new ApiException(ApiErrorCode.FILE_TYPE_INVALID, "Image cannot be decoded");
        }
        if (image.getWidth() > MAX_DIMENSION || image.getHeight() > MAX_DIMENSION
                || (long) image.getWidth() * image.getHeight() > MAX_PIXELS) {
            throw new ApiException(ApiErrorCode.FILE_SIZE_EXCEEDED, "Image dimensions are too large");
        }

        return new ValidatedImage(bytes, format.contentType, "upload." + format.extension,
                format.extension, image.getWidth(), image.getHeight());
    }

    private ImageFormat detectFormat(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return ImageFormat.JPEG;
        }
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50
                && bytes[2] == 0x4e && bytes[3] == 0x47 && bytes[4] == 0x0d
                && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return ImageFormat.PNG;
        }
        throw new ApiException(ApiErrorCode.FILE_TYPE_INVALID, "Only JPEG and PNG images are allowed");
    }

    private String extension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private enum ImageFormat {
        JPEG("jpg", "image/jpeg"),
        PNG("png", "image/png");

        private final String extension;
        private final String contentType;

        ImageFormat(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        private boolean matchesExtension(String value) {
            return this == JPEG ? JPEG_EXTENSIONS.contains(value) : extension.equals(value);
        }
    }

    public record ValidatedImage(byte[] bytes, String contentType, String safeFileName,
                                 String extension, int width, int height) {
    }
}
