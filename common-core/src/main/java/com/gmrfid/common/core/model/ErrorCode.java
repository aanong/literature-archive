package com.gmrfid.common.core.model;

public final class ErrorCode {
  private ErrorCode() {
  }

  public static final String SUCCESS = "0000";
  public static final String INVALID_PARAM = "4001";
  public static final String NOT_FOUND = "4004";
  public static final String CONFLICT = "4009";
  public static final String UNAUTHORIZED = "4010";
  public static final String FORBIDDEN = "4030";
  public static final String INTERNAL_ERROR = "5000";
  public static final String DOWNSTREAM_ERROR = "5001";
}
