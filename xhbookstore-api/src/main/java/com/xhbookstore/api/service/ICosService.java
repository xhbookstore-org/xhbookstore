package com.xhbookstore.api.service;

import java.io.InputStream;
import java.util.Map;

/**
 * 腾讯云COS文件上传服务
 */
public interface ICosService {
    /**
     * 上传文件到COS
     * @param inputStream 文件流
     * @param fileName 原始文件名
     * @param contentType 文件类型
     * @param folder 存储文件夹路径
     * @return {imageId, url, thumbUrl, fileName}
     */
    Map<String, String> upload(InputStream inputStream, String fileName, String contentType, String folder);

    void delete(String key);
}
