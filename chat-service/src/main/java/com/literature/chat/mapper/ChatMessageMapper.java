package com.literature.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.literature.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息 Mapper 接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 分页查询会话消息（按时间倒序）
     */
    IPage<ChatMessage> selectBySessionIdDesc(
            Page<ChatMessage> page,
            @Param("sessionId") Long sessionId);

    /**
     * 查询某时间点之后的消息
     */
    List<ChatMessage> selectAfterTime(
            @Param("sessionId") Long sessionId,
            @Param("afterTime") LocalDateTime afterTime);

    /**
     * 统计会话消息数
     */
    Long countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 统计未读消息数
     */
    Long countUnreadMessages(
            @Param("sessionId") Long sessionId,
            @Param("lastReadAt") LocalDateTime lastReadAt,
            @Param("userId") Long userId);

    /**
     * 搜索消息内容
     */
    IPage<ChatMessage> searchMessages(
            Page<ChatMessage> page,
            @Param("sessionId") Long sessionId,
            @Param("keyword") String keyword);

    /**
     * 批量更新消息状态
     */
    int updateStatusByIds(
            @Param("ids") List<Long> ids,
            @Param("status") String status);

    /**
     * 删除会话的所有消息
     */
    int deleteBySessionId(@Param("sessionId") Long sessionId);
}
