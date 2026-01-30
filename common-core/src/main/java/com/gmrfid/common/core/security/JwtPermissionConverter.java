package com.gmrfid.common.core.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtPermissionConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Object permissions = jwt.getClaim("permissions");
    if (permissions instanceof List<?> permissionList) {
      return permissionList.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toSet());
    }
    return Collections.emptySet();
  }
}
