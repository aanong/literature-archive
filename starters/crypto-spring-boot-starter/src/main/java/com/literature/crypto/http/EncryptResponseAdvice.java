package com.literature.crypto.http;

import org.springframework.core.MethodParameter;

public class EncryptResponseAdvice {
  public boolean shouldEncrypt(MethodParameter returnType) {
    if (returnType == null) {
      return false;
    }
    if (returnType.getContainingClass().isAnnotationPresent(EncryptResponse.class)) {
      return true;
    }
    return returnType.hasMethodAnnotation(EncryptResponse.class);
  }
}
