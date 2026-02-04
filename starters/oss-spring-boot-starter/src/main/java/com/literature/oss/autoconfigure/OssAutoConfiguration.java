package com.literature.oss.autoconfigure;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.literature.oss.client.AliyunOssClient;
import com.literature.oss.client.LocalOssClient;
import com.literature.oss.client.MinioOssClient;
import com.literature.oss.client.QiniuOssClient;
import com.literature.oss.core.OssClient;
import com.literature.oss.core.OssTemplate;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = "literature.oss", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OssAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public OssTemplate ossTemplate(OssClient ossClient, OssProperties properties) {
    return new OssTemplate(ossClient, properties);
  }

  @Configuration
  @ConditionalOnProperty(prefix = "literature.oss", name = "type", havingValue = "local", matchIfMissing = true)
  static class LocalConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public OssClient ossClient(OssProperties properties) {
      return new LocalOssClient(properties);
    }
  }

  @Configuration
  @ConditionalOnProperty(prefix = "literature.oss", name = "type", havingValue = "minio")
  static class MinioConfiguration {
    @Bean
    public MinioClient minioClient(OssProperties properties) {
      return MinioClient.builder()
          .endpoint(properties.getMinio().getEndpoint())
          .credentials(properties.getMinio().getAccessKey(), properties.getMinio().getSecretKey())
          .build();
    }

    @Bean
    public OssClient ossClient(MinioClient minioClient) {
      return new MinioOssClient(minioClient);
    }
  }

  @Configuration
  @ConditionalOnProperty(prefix = "literature.oss", name = "type", havingValue = "aliyun")
  static class AliyunConfiguration {
    @Bean
    public OSS ossClient(OssProperties properties) {
      return new OSSClientBuilder().build(properties.getAliyun().getEndpoint(),
          properties.getAliyun().getAccessKeyId(),
          properties.getAliyun().getAccessKeySecret());
    }

    @Bean
    public OssClient ossClient(OSS ossClient) {
      return new AliyunOssClient(ossClient);
    }
  }

  @Configuration
  @ConditionalOnProperty(prefix = "literature.oss", name = "type", havingValue = "qiniu")
  static class QiniuConfiguration {
    @Bean
    public Auth qiniuAuth(OssProperties properties) {
      return Auth.create(properties.getQiniu().getAccessKey(), properties.getQiniu().getSecretKey());
    }

    @Bean
    public UploadManager uploadManager(OssProperties properties) {
      return new UploadManager(QiniuOssClient.resolveRegion(properties.getQiniu().getRegion()));
    }

    @Bean
    public BucketManager bucketManager(OssProperties properties) {
      return new BucketManager(qiniuAuth(properties), QiniuOssClient.resolveRegion(properties.getQiniu().getRegion()));
    }

    @Bean
    public OssClient ossClient(UploadManager uploadManager, Auth auth, BucketManager bucketManager, OssProperties properties) {
      return new QiniuOssClient(uploadManager, auth, bucketManager, properties.getQiniu().getDomain());
    }
  }
}
