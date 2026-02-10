package com.literature.chat.netty.session;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器
 */
@Slf4j
@Component
public class SessionManager {

    /**
     * userId -> Channel
     */
    private final Map<Long, Channel> userChannelMap = new ConcurrentHashMap<>();

    /**
     * ChannelId -> userId
     */
    private final Map<String, Long> channelUserMap = new ConcurrentHashMap<>();

    public void addSession(Long userId, Channel channel) {
        Channel oldChannel = userChannelMap.put(userId, channel);
        if (oldChannel != null && oldChannel != channel) {
            channelUserMap.remove(oldChannel.id().asLongText());
            oldChannel.close();
            log.warn("用户 {} 重复连接，已关闭旧 Channel {}", userId, oldChannel.id());
        }
        channelUserMap.put(channel.id().asLongText(), userId);
        log.info("用户 {} 已连接, channel: {}", userId, channel.id());
    }

    /**
     * 移除会话映射，返回被移除的 userId（可能为 null）
     */
    public Long removeSession(Channel channel) {
        String channelId = channel.id().asLongText();
        Long userId = channelUserMap.remove(channelId);
        if (userId != null) {
            userChannelMap.remove(userId);
            log.info("用户 {} 已断开, channel: {}", userId, channelId);
        }
        return userId;
    }

    public Channel getChannel(Long userId) {
        return userChannelMap.get(userId);
    }

    public Long getUserId(Channel channel) {
        return channelUserMap.get(channel.id().asLongText());
    }
}
