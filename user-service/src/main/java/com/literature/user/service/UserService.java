package com.literature.user.service;

import com.literature.common.core.dto.UserDTO;
import com.literature.user.mapper.UserAccountMapper;
import com.literature.user.model.UserAccount;
import org.springframework.stereotype.Service;

/**
 * 用户服务 - 提供用户信息查询
 */
@Service
public class UserService {

    private final UserAccountMapper userAccountMapper;

    public UserService(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户DTO，如果不存在返回null
     */
    public UserDTO getUserById(Long userId) {
        UserAccount account = userAccountMapper.selectById(userId);
        if (account == null) {
            return null;
        }
        return toDTO(account);
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户DTO，如果不存在返回null
     */
    public UserDTO getUserByUsername(String username) {
        UserAccount account = userAccountMapper.findByUsername(username);
        if (account == null) {
            return null;
        }
        return toDTO(account);
    }

    /**
     * 将 UserAccount 转换为 UserDTO
     * 注意：不暴露敏感信息如密码哈希
     */
    private UserDTO toDTO(UserAccount account) {
        return new UserDTO(
            account.getId(),
            account.getUsername(),
            account.getUsername(), // 暂用 username 作为 nickname，可后续扩展
            account.getStatus()
        );
    }
}
