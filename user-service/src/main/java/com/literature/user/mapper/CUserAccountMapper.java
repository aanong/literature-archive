package com.literature.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.literature.user.model.CUserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CUserAccountMapper extends BaseMapper<CUserAccount> {

    @Select("SELECT id, username, password_hash AS passwordHash, status, created_at AS createdAt FROM c_users WHERE username = #{username}")
    CUserAccount findByUsername(@Param("username") String username);
}
