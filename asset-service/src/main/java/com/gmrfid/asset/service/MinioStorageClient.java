package com.gmrfid.asset.service;

import com.gmrfid.asset.config.StorageProperties;
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
