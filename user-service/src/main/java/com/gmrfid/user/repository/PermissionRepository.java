package com.gmrfid.user.repository;

import com.gmrfid.user.model.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

  @Query("select p.code from Permission p "
      + "join RolePermission rp on p.id = rp.permissionId "
      + "join UserRole ur on ur.roleId = rp.roleId "
      + "where ur.userId = :userId")
  List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
}
