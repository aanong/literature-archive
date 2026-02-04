package com.literature.oss.client;

import com.literature.oss.core.OssClient;
import com.literature.oss.core.OssException;
import com.literature.oss.model.UploadResult;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

public class QiniuOssClient implements OssClient {
  private final UploadManager uploadManager;
  private final Auth auth;
  private final BucketManager bucketManager;
  private final String domain;

  public QiniuOssClient(UploadManager uploadManager, Auth auth, BucketManager bucketManager, String domain) {
    this.uploadManager = uploadManager;
    this.auth = auth;
    this.bucketManager = bucketManager;
    this.domain = domain;
  }

  @Override
  public UploadResult upload(String bucketName, String objectName, InputStream inputStream, String contentType) {
    try {
      byte[] bytes = inputStream.readAllBytes();
      String token = auth.uploadToken(bucketName);
      Response response = uploadManager.put(bytes, objectName, token);
      if (!response.isOK()) {
        throw new OssException("Qiniu upload failed: " + response.bodyString());
      }
      UploadResult result = new UploadResult();
      result.setBucketName(bucketName);
      result.setObjectName(objectName);
      result.setContentType(contentType);
      result.setUrl(getUrl(bucketName, objectName, 0));
      return result;
    } catch (QiniuException ex) {
      throw new OssException("Qiniu upload failed", ex);
    } catch (Exception ex) {
      throw new OssException("Qiniu upload failed", ex);
    }
  }

  @Override
  public InputStream download(String bucketName, String objectName) {
    try {
      String url = getUrl(bucketName, objectName, 3600);
      return new URL(url).openStream();
    } catch (Exception ex) {
      throw new OssException("Qiniu download failed", ex);
    }
  }

  @Override
  public void delete(String bucketName, String objectName) {
    try {
      bucketManager.delete(bucketName, objectName);
    } catch (QiniuException ex) {
      throw new OssException("Qiniu delete failed", ex);
    }
  }

  @Override
  public String getUrl(String bucketName, String objectName, int expireSeconds) {
    try {
      if (domain == null || domain.isBlank()) {
        throw new OssException("Qiniu domain is required");
      }
      DownloadUrl url = new DownloadUrl(domain, false, objectName);
      long deadline = System.currentTimeMillis() / 1000 + (expireSeconds > 0 ? expireSeconds : 3600 * 24 * 7);
      return url.buildURL(auth, deadline);
    } catch (Exception ex) {
      throw new OssException("Qiniu get url failed", ex);
    }
  }

  @Override
  public boolean exists(String bucketName, String objectName) {
    try {
      bucketManager.stat(bucketName, objectName);
      return true;
    } catch (QiniuException ex) {
      return false;
    }
  }

  public static Configuration resolveRegion(String region) {
    if (region == null || region.isBlank()) {
      return new Configuration(Region.autoRegion());
    }
    return switch (region) {
      case "z0" -> new Configuration(Region.huadong());
      case "z1" -> new Configuration(Region.huabei());
      case "z2" -> new Configuration(Region.huanan());
      case "na0" -> new Configuration(Region.beimei());
      case "as0" -> new Configuration(Region.xinjiapo());
      default -> new Configuration(Region.autoRegion());
    };
  }
}

