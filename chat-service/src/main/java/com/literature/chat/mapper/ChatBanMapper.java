package com.literature.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.literature.chat.entity.ChatBan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 封禁记录 Mapper 接口
 */
@Mapper
public interface ChatBanMapper extends BaseMapper<ChatBan> {

    /**
     * 分页查询会话封禁记录
     */
    IPage<ChatBan> selectBySessionId(
            Page<ChatBan> page,
            @Param("sessionId") Long sessionId);

    /**
     * 分页查询会话中指定状态的封禁记录
     */
    IPage<ChatBan> selectBySessionIdAndStatus(
            Page<ChatBan> page,
            @Param("sessionId") Long sessionId,
            @Param("status") String status);

    /**
     * 查询某会话中某用户的有效封禁记录
     */
    ChatBan selectActiveBan(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId);

    /**
     * 检查用户在某会话中是否被封禁
     */
    boolean isUserBanned(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId);

    /**
     * 查询某会话中所有有效的封禁记录
     */
    List<ChatBan> selectActiveBansBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 统计某会话的有效封禁数
     */
    Long countActiveBans(@Param("sessionId") Long sessionId);

    /**
     * 批量解除封禁
     */
    int liftBans(
            @Param("ids") List<Long> ids,
            @Param("operatorId") Long operatorId);
}
