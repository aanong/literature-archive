package com.literature.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.literature.chat.entity.ChatMute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 禁言记录 Mapper 接口
 */
@Mapper
public interface ChatMuteMapper extends BaseMapper<ChatMute> {

    /**
     * 查询某会话中某用户的有效禁言记录
     */
    ChatMute selectActiveMute(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    /**
     * 检查用户在某会话中是否被禁言
     */
    boolean isUserMuted(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    /**
     * 查询某会话中所有有效的禁言记录
     */
    List<ChatMute> selectActiveMutesBySessionId(
            @Param("sessionId") Long sessionId,
            @Param("now") LocalDateTime now);

    /**
     * 统计某会话的有效禁言数
     */
    Long countActiveMutes(
            @Param("sessionId") Long sessionId,
            @Param("now") LocalDateTime now);

    /**
     * 删除过期的禁言记录
     */
    int deleteExpiredMutes(@Param("before") LocalDateTime before);
}
