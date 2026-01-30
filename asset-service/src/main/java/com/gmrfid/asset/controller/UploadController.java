package com.gmrfid.asset.controller;

import com.gmrfid.asset.model.UploadResponse;
import com.gmrfid.asset.service.StorageClient;
import com.gmrfid.common.core.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/assets")
public class UploadController {
  private final StorageClient storageClient;

  public UploadController(StorageClient storageClient) {
    this.storageClient = storageClient;
  }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file,
      HttpServletRequest request) throws IOException {
    String objectKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
    String url = storageClient.upload(objectKey, file.getInputStream(), file.getContentType());
    return ApiResponse.success(new UploadResponse(objectKey, url), request.getHeader("X-Trace-Id"));
  }
}
