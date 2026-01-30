package com.gmrfid.common.core.security;

import java.util.Collection;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("permissionGuard")
public class PermissionGuard {
  public boolean hasPermission(Authentication authentication, String permission) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    return authorities != null && authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(Objects::nonNull)
        .anyMatch(permission::equals);
  }
}
