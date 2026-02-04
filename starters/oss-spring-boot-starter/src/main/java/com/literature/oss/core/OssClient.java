package com.literature.oss.core;

import com.literature.oss.model.UploadResult;

import java.io.InputStream;

public interface OssClient {
  UploadResult upload(String bucketName, String objectName, InputStream inputStream, String contentType);

  InputStream download(String bucketName, String objectName);

  void delete(String bucketName, String objectName);

  String getUrl(String bucketName, String objectName, int expireSeconds);

  boolean exists(String bucketName, String objectName);
}
