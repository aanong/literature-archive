package com.gmrfid.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "user_roles")
@IdClass(UserRole.UserRoleId.class)
public class UserRole {
  @Id
  @Column(name = "user_id")
  private Long userId;

  @Id
  @Column(name = "role_id")
  private Long roleId;

  public Long getUserId() {
    return userId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public static class UserRoleId implements Serializable {
    private Long userId;
    private Long roleId;

    public UserRoleId() {
    }

    public UserRoleId(Long userId, Long roleId) {
      this.userId = userId;
      this.roleId = roleId;
    }
  }
}
