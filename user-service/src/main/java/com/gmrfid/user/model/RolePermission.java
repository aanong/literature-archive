package com.gmrfid.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermission.RolePermissionId.class)
public class RolePermission {
  @Id
  @Column(name = "role_id")
  private Long roleId;

  @Id
  @Column(name = "permission_id")
  private Long permissionId;

  public Long getRoleId() {
    return roleId;
  }

  public Long getPermissionId() {
    return permissionId;
  }

  public static class RolePermissionId implements Serializable {
    private Long roleId;
    private Long permissionId;

    public RolePermissionId() {
    }

    public RolePermissionId(Long roleId, Long permissionId) {
      this.roleId = roleId;
      this.permissionId = permissionId;
    }
  }
}
