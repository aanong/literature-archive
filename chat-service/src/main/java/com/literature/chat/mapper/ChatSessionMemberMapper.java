package com.literature.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.literature.chat.entity.ChatSessionMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话成员 Mapper 接口
 */
@Mapper
public interface ChatSessionMemberMapper extends BaseMapper<ChatSessionMember> {

    /**
     * 根据会话ID查询所有成员
     */
    List<ChatSessionMember> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询会话成员用户ID列表
     */
    List<Long> selectUserIdsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 统计会话成员数
     */
    Long countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询特定用户在会话中的成员记录
     */
    ChatSessionMember selectBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId);

    /**
     * 检查用户是否是会话成员
     */
    boolean existsBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId);

    /**
     * 删除会话成员
     */
    int deleteBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId);
}
