package com.wanshu.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.wanshu.common.config.OssConfig;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云 OSS 工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    private final OssConfig ossConfig;

    /**
     * 上传文件
     *
     * @param inputStream 文件流
     * @param originalFilename 原始文件名
     * @return 文件访问 URL
     */
    public String upload(InputStream inputStream, String originalFilename) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            // 生成存储路径: yyyy/MM/dd/uuid.ext
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectKey = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(ext));

            ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream, metadata);

            return ossConfig.getUrlPrefix() + "/" + objectKey;
        } catch (Exception e) {
            log.error("OSS 文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.OSS_UPLOAD_ERROR);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件完整 URL
     */
    public void delete(String fileUrl) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            String objectKey = fileUrl.replace(ossConfig.getUrlPrefix() + "/", "");
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("OSS 文件删除失败: {}", e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 生成临时签名 URL（用于前端直传或临时访问私有文件）
     *
     * @param objectKey 文件 Key
     * @param expireSeconds 过期秒数
     * @return 签名 URL
     */
    public String generatePresignedUrl(String objectKey, long expireSeconds) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
            URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), objectKey, expiration);
            return url.toString();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private String getContentType(String ext) {
        return switch (ext.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}
