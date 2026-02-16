package com.literature.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public String encode(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }

  public boolean matches(String rawPassword, String hash) {
    return passwordEncoder.matches(rawPassword, hash);
  }

  public static void main(String[] args) {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    ;
    System.out.println(passwordEncoder.encode("admin123"));
  }
}
