package com.literature.oss.client;

import com.literature.oss.autoconfigure.OssProperties;
import com.literature.oss.core.OssClient;
import com.literature.oss.core.OssException;
import com.literature.oss.model.UploadResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LocalOssClient implements OssClient {
  private final OssProperties properties;

  public LocalOssClient(OssProperties properties) {
    this.properties = properties;
  }

  @Override
  public UploadResult upload(String bucketName, String objectName, InputStream inputStream, String contentType) {
    Path bucketPath = resolveBucketPath(bucketName);
    Path objectPath = bucketPath.resolve(objectName);
    try {
      Files.createDirectories(objectPath.getParent());
      Files.copy(inputStream, objectPath, StandardCopyOption.REPLACE_EXISTING);
      UploadResult result = new UploadResult();
      result.setBucketName(bucketName);
      result.setObjectName(objectName);
      result.setContentType(contentType);
      result.setSize(Files.size(objectPath));
      result.setUrl(getUrl(bucketName, objectName, 0));
      return result;
    } catch (IOException ex) {
      throw new OssException("Local upload failed", ex);
    }
  }

  @Override
  public InputStream download(String bucketName, String objectName) {
    Path objectPath = resolveBucketPath(bucketName).resolve(objectName);
    if (!Files.exists(objectPath)) {
      throw new OssException("Object not found: " + objectName);
    }
    try {
      return Files.newInputStream(objectPath);
    } catch (IOException ex) {
      throw new OssException("Local download failed", ex);
    }
  }

  @Override
  public void delete(String bucketName, String objectName) {
    Path objectPath = resolveBucketPath(bucketName).resolve(objectName);
    try {
      Files.deleteIfExists(objectPath);
    } catch (IOException ex) {
      throw new OssException("Local delete failed", ex);
    }
  }

  @Override
  public String getUrl(String bucketName, String objectName, int expireSeconds) {
    String prefix = properties.getLocal().getUrlPrefix();
    if (prefix == null || prefix.isBlank()) {
      return objectName;
    }
    String normalized = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    return normalized + "/" + bucketName + "/" + objectName;
  }

  @Override
  public boolean exists(String bucketName, String objectName) {
    return Files.exists(resolveBucketPath(bucketName).resolve(objectName));
  }

  private Path resolveBucketPath(String bucketName) {
    String rootPath = properties.getLocal().getRootPath();
    if (rootPath == null || rootPath.isBlank()) {
      throw new OssException("Local root path is required");
    }
    return Path.of(rootPath).resolve(bucketName);
  }
}
