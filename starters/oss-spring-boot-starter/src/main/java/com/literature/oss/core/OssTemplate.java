package com.literature.oss.core;

import com.literature.oss.autoconfigure.OssProperties;
import com.literature.oss.model.UploadResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class OssTemplate {
  private final OssClient ossClient;
  private final OssProperties properties;

  public OssTemplate(OssClient ossClient, OssProperties properties) {
    this.ossClient = ossClient;
    this.properties = properties;
  }

  public UploadResult upload(MultipartFile file) {
    return upload(properties.getDefaultBucket(), file);
  }

  public UploadResult upload(String bucketName, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new OssException("Upload file is empty");
    }
    String resolvedBucket = resolveBucket(bucketName);
    String objectName = generateObjectName(file.getOriginalFilename());
    try {
      return ossClient.upload(resolvedBucket, objectName, file.getInputStream(), file.getContentType());
    } catch (Exception ex) {
      throw new OssException("Upload failed", ex);
    }
  }

  public List<UploadResult> uploadBatch(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return List.of();
    }
    List<UploadResult> results = new ArrayList<>(files.size());
    for (MultipartFile file : files) {
      results.add(upload(file));
    }
    return results;
  }

  public String generateObjectName(String originalFilename) {
    String safeName = StringUtils.hasText(originalFilename) ? originalFilename : "file";
    String suffix = "";
    int dotIndex = safeName.lastIndexOf('.');
    if (dotIndex >= 0 && dotIndex < safeName.length() - 1) {
      suffix = safeName.substring(dotIndex);
      safeName = safeName.substring(0, dotIndex);
    }
    String cleanBase = safeName.replaceAll("\\s+", "-");
    return String.format("%tF/%s-%s%s", java.time.LocalDate.now(), cleanBase, UUID.randomUUID(), suffix);
  }

  public boolean exists(String bucketName, String objectName) {
    return ossClient.exists(resolveBucket(bucketName), objectName);
  }

  public String getUrl(String bucketName, String objectName, int expireSeconds) {
    return ossClient.getUrl(resolveBucket(bucketName), objectName, expireSeconds);
  }

  private String resolveBucket(String bucketName) {
    String resolved = StringUtils.hasText(bucketName) ? bucketName : properties.getDefaultBucket();
    if (!StringUtils.hasText(resolved)) {
      throw new OssException("Bucket name is required");
    }
    return resolved;
  }
}
