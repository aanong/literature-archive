package com.literature.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.literature.chat.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 聊天会话 Mapper 接口
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 分页搜索会话
     *
     * @param page    分页参数
     * @param keyword 关键词
     * @param status  状态
     * @return 分页结果
     */
    IPage<ChatSession> searchSessions(
            Page<ChatSession> page,
            @Param("keyword") String keyword,
            @Param("status") String status);

    /**
     * 查询用户参与的会话
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @param status 状态
     * @return 分页结果
     */
    IPage<ChatSession> selectByMemberUserId(
            Page<ChatSession> page,
            @Param("userId") Long userId,
            @Param("status") String status);

    /**
     * 查询两个用户之间的私聊会话
     *
     * @param user1 用户1
     * @param user2 用户2
     * @return 私聊会话
     */
    ChatSession selectPrivateSession(
            @Param("user1") Long user1,
            @Param("user2") Long user2);
}
