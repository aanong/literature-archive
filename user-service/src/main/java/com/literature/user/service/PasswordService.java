package com.literature.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public boolean matches(String rawPassword, String hash) {
    return passwordEncoder.matches(rawPassword, hash);
  }
}
