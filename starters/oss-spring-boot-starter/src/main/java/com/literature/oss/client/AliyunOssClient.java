package com.literature.oss.client;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.literature.oss.core.OssClient;
import com.literature.oss.core.OssException;
import com.literature.oss.model.UploadResult;
import java.io.InputStream;
import java.util.Date;

public class AliyunOssClient implements OssClient {
  private final OSS ossClient;

  public AliyunOssClient(OSS ossClient) {
    this.ossClient = ossClient;
  }

  @Override
  public UploadResult upload(String bucketName, String objectName, InputStream inputStream, String contentType) {
    try {
      PutObjectRequest request = new PutObjectRequest(bucketName, objectName, inputStream);
      if (contentType != null && !contentType.isBlank()) {
        request.getMetadata().setContentType(contentType);
      }
      ossClient.putObject(request);
      UploadResult result = new UploadResult();
      result.setBucketName(bucketName);
      result.setObjectName(objectName);
      result.setContentType(contentType);
      result.setUrl(getUrl(bucketName, objectName, 0));
      return result;
    } catch (Exception ex) {
      throw new OssException("Aliyun upload failed", ex);
    }
  }

  @Override
  public InputStream download(String bucketName, String objectName) {
    try {
      OSSObject object = ossClient.getObject(bucketName, objectName);
      return object.getObjectContent();
    } catch (Exception ex) {
      throw new OssException("Aliyun download failed", ex);
    }
  }

  @Override
  public void delete(String bucketName, String objectName) {
    try {
      ossClient.deleteObject(bucketName, objectName);
    } catch (Exception ex) {
      throw new OssException("Aliyun delete failed", ex);
    }
  }

  @Override
  public String getUrl(String bucketName, String objectName, int expireSeconds) {
    try {
      if (expireSeconds <= 0) {
        expireSeconds = 3600;
      }
      Date expires = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
      GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
      request.setExpiration(expires);
      return ossClient.generatePresignedUrl(request).toString();
    } catch (Exception ex) {
      throw new OssException("Aliyun get url failed", ex);
    }
  }

  @Override
  public boolean exists(String bucketName, String objectName) {
    try {
      return ossClient.doesObjectExist(bucketName, objectName);
    } catch (Exception ex) {
      return false;
    }
  }
}
