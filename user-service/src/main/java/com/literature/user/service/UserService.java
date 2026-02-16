package com.literature.user.service;

import com.literature.common.core.dto.UserDTO;
import com.literature.user.mapper.CUserAccountMapper;
import com.literature.user.mapper.UserAccountMapper;
import com.literature.user.model.CUserAccount;
import com.literature.user.model.UserAccount;
import org.springframework.stereotype.Service;

/**
 * 用户服务 - 提供用户信息查询
 */
@Service
public class UserService {

    private final UserAccountMapper userAccountMapper;
    private final CUserAccountMapper cUserAccountMapper;

    public UserService(UserAccountMapper userAccountMapper, CUserAccountMapper cUserAccountMapper) {
        this.userAccountMapper = userAccountMapper;
        this.cUserAccountMapper = cUserAccountMapper;
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户DTO，如果不存在返回null
     */
    public UserDTO getUserById(Long userId, String userType) {
        if ("ADMIN".equalsIgnoreCase(userType)) {
            return getAdminUserById(userId);
        }
        if ("C_USER".equalsIgnoreCase(userType)) {
            return getCUserById(userId);
        }
        UserDTO admin = getAdminUserById(userId);
        if (admin != null) {
            return admin;
        }
        return getCUserById(userId);
    }

    public UserDTO getUserByUsername(String username, String userType) {
        if ("ADMIN".equalsIgnoreCase(userType)) {
            return getAdminUserByUsername(username);
        }
        if ("C_USER".equalsIgnoreCase(userType)) {
            return getCUserByUsername(username);
        }
        UserDTO admin = getAdminUserByUsername(username);
        if (admin != null) {
            return admin;
        }
        return getCUserByUsername(username);
    }

    public UserDTO getAdminUserById(Long userId) {
        UserAccount account = userAccountMapper.selectById(userId);
        if (account == null) {
            return null;
        }
        return toAdminDTO(account);
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户DTO，如果不存在返回null
     */
    public UserDTO getAdminUserByUsername(String username) {
        UserAccount account = userAccountMapper.findByUsername(username);
        if (account == null) {
            return null;
        }
        return toAdminDTO(account);
    }

    public UserDTO getCUserById(Long userId) {
        CUserAccount account = cUserAccountMapper.selectById(userId);
        if (account == null) {
            return null;
        }
        return toCUserDTO(account);
    }

    public UserDTO getCUserByUsername(String username) {
        CUserAccount account = cUserAccountMapper.findByUsername(username);
        if (account == null) {
            return null;
        }
        return toCUserDTO(account);
    }

    /**
     * 将 UserAccount 转换为 UserDTO
     * 注意：不暴露敏感信息如密码哈希
     */
    private UserDTO toAdminDTO(UserAccount account) {
        return new UserDTO(
            account.getId(),
            account.getUsername(),
            account.getUsername(), // 暂用 username 作为 nickname，可后续扩展
            account.getStatus(),
            "ADMIN"
        );
    }

    private UserDTO toCUserDTO(CUserAccount account) {
        return new UserDTO(
            account.getId(),
            account.getUsername(),
            account.getUsername(),
            account.getStatus(),
            "C_USER"
        );
    }
}
