package com.literature.asset.service;

import com.literature.asset.config.StorageProperties;
import java.io.InputStream;
import org.springframework.stereotype.Service;

@Service
public class MinioStorageClient implements StorageClient {
  private final StorageProperties properties;

  public MinioStorageClient(StorageProperties properties) {
    this.properties = properties;
  }

  @Override
  public String upload(String objectKey, InputStream inputStream, String contentType) {
    return properties.getEndpoint() + "/" + properties.getBucket() + "/" + objectKey;
  }
}
