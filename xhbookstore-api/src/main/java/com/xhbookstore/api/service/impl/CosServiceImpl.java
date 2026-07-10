package com.xhbookstore.api.service.impl;

import java.io.InputStream;
import java.util.*;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhbookstore.api.config.CosConfig;
import com.xhbookstore.api.service.ICosService;

@Service
public class CosServiceImpl implements ICosService {

    private static final Logger log = LoggerFactory.getLogger(CosServiceImpl.class);

    @Autowired
    private CosConfig cosConfig;

    @Override
    public Map<String, String> upload(InputStream inputStream, String fileName, String contentType, String folder) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            String imageId = UUID.randomUUID().toString().replace("-", "");
            String ext = getFileExt(fileName);
            String key = folder + "/" + imageId + ext;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType != null ? contentType : "image/jpeg");

            PutObjectRequest putRequest = new PutObjectRequest(cosConfig.getBucket(), key, inputStream, metadata);
            cosClient.putObject(putRequest);

            String url = trimTrailingSlash(cosConfig.getBaseUrl()) + "/" + key;
            String thumbUrl = url + "?imageMogr2/thumbnail/400x";

            Map<String, String> result = new HashMap<>();
            result.put("imageId", imageId);
            result.put("url", url);
            result.put("thumbUrl", thumbUrl);
            result.put("fileName", fileName);
            result.put("key", key);
            return result;
        } catch (Exception e) {
            log.error("COS upload failed: {}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            cosClient.shutdown();
        }
    }

    private String getFileExt(String fileName) {
        if (fileName == null || !fileName.contains(".")) return ".jpg";
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String trimTrailingSlash(String value) {
        if (value == null) return "";
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
