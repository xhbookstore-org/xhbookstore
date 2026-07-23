package com.xhbookstore.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.xhbookstore.web.config.AdminCosConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端 COS 文件服务。返回可直接访问的完整 URL，供业务表落库。
 */
@Service
public class AdminCosService
{
    private final AdminCosConfig cosConfig;

    public AdminCosService(AdminCosConfig cosConfig)
    {
        this.cosConfig = cosConfig;
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException
    {
        validateConfig();
        String key = normalizeFolder(folder) + "/" + UUID.randomUUID().toString().replace("-", "")
                + extension(file.getOriginalFilename());
        COSCredentials credentials = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        COSClient client = new COSClient(credentials, new ClientConfig(new Region(cosConfig.getRegion())));
        try (InputStream inputStream = file.getInputStream())
        {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            client.putObject(new PutObjectRequest(cosConfig.getBucket(), key, inputStream, metadata));
            return trimTrailingSlash(cosConfig.getBaseUrl()) + "/" + key;
        }
        finally
        {
            client.shutdown();
        }
    }

    private void validateConfig()
    {
        if (isBlank(cosConfig.getSecretId()) || isBlank(cosConfig.getSecretKey())
                || isBlank(cosConfig.getRegion()) || isBlank(cosConfig.getBucket()) || isBlank(cosConfig.getBaseUrl()))
        {
            throw new IllegalStateException("COS upload configuration is incomplete");
        }
    }

    private String normalizeFolder(String folder)
    {
        String value = folder == null ? "" : folder.replaceAll("^/+|/+$", "");
        return value.isEmpty() ? "bookstore" : value;
    }

    private String extension(String fileName)
    {
        if (fileName == null || !fileName.contains("."))
        {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    }

    private String trimTrailingSlash(String value)
    {
        String result = value;
        while (result.endsWith("/"))
        {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}
