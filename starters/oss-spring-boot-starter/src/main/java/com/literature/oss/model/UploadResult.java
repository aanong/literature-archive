package com.literature.oss.model;

public class UploadResult {
  private String bucketName;
  private String objectName;
  private String url;
  private long size;
  private String contentType;

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}
