package com.gmrfid.asset.service;

import java.io.InputStream;

public interface StorageClient {
  String upload(String objectKey, InputStream inputStream, String contentType);
}
