package com.literature.chat.service;

import com.literature.chat.dto.BanRequestDTO;
import com.literature.chat.dto.MuteRequestDTO;
import com.literature.chat.dto.SessionQueryDTO;
import com.literature.chat.vo.BanVO;
import com.literature.chat.vo.MessageVO;
import com.literature.chat.vo.MuteVO;
import com.literature.chat.vo.SessionVO;
import com.literature.common.core.model.PageResponse;

/**
 * 聊天会话服务接口
 */
public interface ChatSessionService {

    /**
     * 分页查询会话列表
     *
     * @param query 查询参数
     * @return 会话分页数据
     */
    PageResponse<SessionVO> listSessions(SessionQueryDTO query);

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话详情
     */
    SessionVO getSession(Long sessionId);

    /**
     * 分页查询会话消息
     *
     * @param sessionId 会话ID
     * @param page      页码
     * @param pageSize  每页数量
     * @return 消息分页数据
     */
    PageResponse<MessageVO> listMessages(Long sessionId, int page, int pageSize);

    /**
     * 禁言用户
     *
     * @param sessionId  会话ID
     * @param request    禁言请求
     * @param operatorId 操作者ID
     * @return 禁言信息
     */
    MuteVO muteUser(Long sessionId, MuteRequestDTO request, Long operatorId);

    /**
     * 解除禁言
     *
     * @param sessionId  会话ID
     * @param userId     用户ID
     * @param operatorId 操作者ID
     */
    void unmuteUser(Long sessionId, Long userId, Long operatorId);

    /**
     * 封禁用户
     *
     * @param sessionId  会话ID
     * @param request    封禁请求
     * @param operatorId 操作者ID
     * @return 封禁信息
     */
    BanVO banUser(Long sessionId, BanRequestDTO request, Long operatorId);

    /**
     * 解除封禁
     *
     * @param sessionId  会话ID
     * @param userId     用户ID
     * @param operatorId 操作者ID
     */
    void unbanUser(Long sessionId, Long userId, Long operatorId);

    /**
     * 检查用户是否被禁言
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 是否被禁言
     */
    boolean isUserMuted(Long sessionId, Long userId);

    /**
     * 检查用户是否被封禁
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 是否被封禁
     */
    boolean isUserBanned(Long sessionId, Long userId);

    /**
     * 获取会话成员ID列表
     *
     * @param sessionId 会话ID
     * @return 成员ID列表
     */
    java.util.List<Long> getSessionMemberIds(Long sessionId);
}
