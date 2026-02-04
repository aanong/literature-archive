package com.literature.oss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.literature.oss.autoconfigure.OssProperties;
import com.literature.oss.client.LocalOssClient;
import com.literature.oss.core.OssException;
import com.literature.oss.core.OssTemplate;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OssTemplateTest {

  @TempDir
  Path tempDir;

  @Test
  void resolvesBucketFromDefaults() {
    OssProperties properties = new OssProperties();
    properties.getLocal().setRootPath(tempDir.toString());
    properties.setDefaultBucket("literature");
    OssTemplate template = new OssTemplate(new LocalOssClient(properties), properties);

    assertThat(template.getUrl(null, "object", 0)).contains("object");
  }

  @Test
  void missingBucketThrows() {
    OssProperties properties = new OssProperties();
    properties.getLocal().setRootPath(tempDir.toString());
    OssTemplate template = new OssTemplate(new LocalOssClient(properties), properties);

    assertThatThrownBy(() -> template.getUrl(null, "object", 0))
        .isInstanceOf(OssException.class)
        .hasMessageContaining("Bucket name is required");
  }
}
