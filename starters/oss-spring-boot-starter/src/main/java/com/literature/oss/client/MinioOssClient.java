package com.literature.oss.client;

import com.literature.oss.core.OssClient;
import com.literature.oss.core.OssException;
import com.literature.oss.model.UploadResult;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MinioOssClient implements OssClient {
  private final MinioClient minioClient;

  public MinioOssClient(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  @Override
  public UploadResult upload(String bucketName, String objectName, InputStream inputStream, String contentType) {
    try {
      PutObjectArgs args = PutObjectArgs.builder()
          .bucket(bucketName)
          .object(objectName)
          .stream(inputStream, -1, 10 * 1024 * 1024)
          .contentType(contentType)
          .build();
      minioClient.putObject(args);
      UploadResult result = new UploadResult();
      result.setBucketName(bucketName);
      result.setObjectName(objectName);
      result.setContentType(contentType);
      result.setUrl(getUrl(bucketName, objectName, 0));
      return result;
    } catch (Exception ex) {
      throw new OssException("MinIO upload failed", ex);
    }
  }

  @Override
  public InputStream download(String bucketName, String objectName) {
    try {
      return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    } catch (Exception ex) {
      throw new OssException("MinIO download failed", ex);
    }
  }

  @Override
  public void delete(String bucketName, String objectName) {
    try {
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    } catch (Exception ex) {
      throw new OssException("MinIO delete failed", ex);
    }
  }

  @Override
  public String getUrl(String bucketName, String objectName, int expireSeconds) {
    try {
      if (expireSeconds <= 0) {
        expireSeconds = (int) TimeUnit.DAYS.toSeconds(7);
      }
      return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .method(Method.GET)
          .bucket(bucketName)
          .object(objectName)
          .expiry(expireSeconds, TimeUnit.SECONDS)
          .build());
    } catch (Exception ex) {
      throw new OssException("MinIO get url failed", ex);
    }
  }

  @Override
  public boolean exists(String bucketName, String objectName) {
    try {
      minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
