package com.literature.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.literature.user.model.Permission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
}
