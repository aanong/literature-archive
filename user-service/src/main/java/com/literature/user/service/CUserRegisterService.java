package com.literature.user.service;

import com.literature.user.mapper.CUserAccountMapper;
import com.literature.user.model.CUserAccount;
import com.literature.user.model.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class CUserRegisterService {

  private final CUserAccountMapper cUserAccountMapper;
  private final PasswordService passwordService;

  public CUserRegisterService(CUserAccountMapper cUserAccountMapper, PasswordService passwordService) {
    this.cUserAccountMapper = cUserAccountMapper;
    this.passwordService = passwordService;
  }

  public void register(RegisterRequest request) {
    String username = request.username().trim();
    if (cUserAccountMapper.findByUsername(username) != null) {
      throw new IllegalArgumentException("用户名已存在");
    }

    CUserAccount user = new CUserAccount();
    user.setUsername(username);
    user.setPasswordHash(passwordService.encode(request.password()));
    user.setStatus("ACTIVE");
    cUserAccountMapper.insert(user);
  }
}
