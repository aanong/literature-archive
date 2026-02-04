package com.literature.oss.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "literature.oss")
public class OssProperties {
  private boolean enabled = true;
  private String type = "local";
  private String defaultBucket;
  private final Local local = new Local();
  private final Minio minio = new Minio();
  private final Aliyun aliyun = new Aliyun();
  private final Qiniu qiniu = new Qiniu();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDefaultBucket() {
    return defaultBucket;
  }

  public void setDefaultBucket(String defaultBucket) {
    this.defaultBucket = defaultBucket;
  }

  public Local getLocal() {
    return local;
  }

  public Minio getMinio() {
    return minio;
  }

  public Aliyun getAliyun() {
    return aliyun;
  }

  public Qiniu getQiniu() {
    return qiniu;
  }

  public static class Local {
    private String rootPath = "uploads";
    private String urlPrefix = "";

    public String getRootPath() {
      return rootPath;
    }

    public void setRootPath(String rootPath) {
      this.rootPath = rootPath;
    }

    public String getUrlPrefix() {
      return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
      this.urlPrefix = urlPrefix;
    }
  }

  public static class Minio {
    private String endpoint;
    private String accessKey;
    private String secretKey;

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public String getAccessKey() {
      return accessKey;
    }

    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }
  }

  public static class Aliyun {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
      return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
      this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
      return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
      this.accessKeySecret = accessKeySecret;
    }
  }

  public static class Qiniu {
    private String accessKey;
    private String secretKey;
    private String domain;
    private String region;

    public String getAccessKey() {
      return accessKey;
    }

    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

    public String getDomain() {
      return domain;
    }

    public void setDomain(String domain) {
      this.domain = domain;
    }

    public String getRegion() {
      return region;
    }

    public void setRegion(String region) {
      this.region = region;
    }
  }
}
