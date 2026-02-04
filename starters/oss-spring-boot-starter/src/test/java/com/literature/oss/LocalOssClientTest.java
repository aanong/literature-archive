package com.literature.oss;

import static org.assertj.core.api.Assertions.assertThat;

import com.literature.oss.autoconfigure.OssProperties;
import com.literature.oss.client.LocalOssClient;
import com.literature.oss.core.OssClient;
import com.literature.oss.core.OssTemplate;
import com.literature.oss.model.UploadResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalOssClientTest {

  @TempDir
  Path tempDir;

  @Test
  void uploadDownloadDeleteFlow() throws Exception {
    OssProperties properties = new OssProperties();
    properties.getLocal().setRootPath(tempDir.toString());
    properties.setDefaultBucket("literature");
    OssClient client = new LocalOssClient(properties);

    UploadResult result = client.upload("literature", "sample.txt",
        new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)), "text/plain");

    assertThat(result.getBucketName()).isEqualTo("literature");
    assertThat(result.getObjectName()).isEqualTo("sample.txt");
    assertThat(client.exists("literature", "sample.txt")).isTrue();
    assertThat(new String(client.download("literature", "sample.txt").readAllBytes(), StandardCharsets.UTF_8))
        .isEqualTo("hello");

    client.delete("literature", "sample.txt");
    assertThat(client.exists("literature", "sample.txt")).isFalse();
    assertThat(Files.exists(tempDir.resolve("literature").resolve("sample.txt"))).isFalse();
  }

  @Test
  void templateGeneratesObjectName() {
    OssProperties properties = new OssProperties();
    properties.getLocal().setRootPath(tempDir.toString());
    properties.setDefaultBucket("literature");
    OssTemplate template = new OssTemplate(new LocalOssClient(properties), properties);

    String objectName = template.generateObjectName("cover.png");

    assertThat(objectName).contains(".png");
    assertThat(objectName).contains("/");
  }
}
