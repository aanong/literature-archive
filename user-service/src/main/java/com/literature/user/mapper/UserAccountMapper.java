package com.literature.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.literature.user.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    @Select("SELECT id, username, password_hash AS passwordHash, status, created_at AS createdAt FROM users WHERE username = #{username}")
    UserAccount findByUsername(@Param("username") String username);
}
