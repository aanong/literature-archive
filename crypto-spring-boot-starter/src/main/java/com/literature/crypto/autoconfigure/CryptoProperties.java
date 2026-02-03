package com.literature.crypto.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "literature.crypto")
public class CryptoProperties {
  private boolean enabled = true;
  private String algorithm = "AES-256-GCM";
  private final Http http = new Http();
  private final Netty netty = new Netty();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public Http getHttp() {
    return http;
  }

  public Netty getNetty() {
    return netty;
  }

  public static class Http {
    private boolean enabled = true;
    private String signKey;
    private String encryptKey;
    private int replayWindowSeconds = 300;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getSignKey() {
      return signKey;
    }

    public void setSignKey(String signKey) {
      this.signKey = signKey;
    }

    public String getEncryptKey() {
      return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
      this.encryptKey = encryptKey;
    }

    public int getReplayWindowSeconds() {
      return replayWindowSeconds;
    }

    public void setReplayWindowSeconds(int replayWindowSeconds) {
      this.replayWindowSeconds = replayWindowSeconds;
    }
  }

  public static class Netty {
    private boolean enabled = true;
    private String keyDeriveSalt;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getKeyDeriveSalt() {
      return keyDeriveSalt;
    }

    public void setKeyDeriveSalt(String keyDeriveSalt) {
      this.keyDeriveSalt = keyDeriveSalt;
    }
  }
}
