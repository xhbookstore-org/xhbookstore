package com.xhbookstore.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.xhbookstore.api.exception.ApiException;

class ImageUploadValidatorTest {
    private final ImageUploadValidator validator = new ImageUploadValidator();

    @Test
    void acceptsDecodedJpegAndUsesDetectedContentType() throws Exception {
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "application/octet-stream", output.toByteArray());

        ImageUploadValidator.ValidatedImage result = validator.validate(file);

        assertThat(result.contentType()).isEqualTo("image/jpeg");
        assertThat(result.safeFileName()).isEqualTo("upload.jpg");
        assertThat(result.width()).isEqualTo(20);
        assertThat(result.height()).isEqualTo(10);
    }

    @Test
    void rejectsFakeImageEvenWhenClientMimeIsImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.jpg", "image/jpeg", "not-an-image".getBytes());

        assertThatThrownBy(() -> validator.validate(file)).isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsExtensionThatDoesNotMatchSignature() throws Exception {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", output.toByteArray());

        assertThatThrownBy(() -> validator.validate(file)).isInstanceOf(ApiException.class);
    }
}
