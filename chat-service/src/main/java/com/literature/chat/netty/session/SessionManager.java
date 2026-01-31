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
        userChannelMap.put(userId, channel);
        channelUserMap.put(channel.id().asLongText(), userId);
        log.info("User {} connected, channel: {}", userId, channel.id());
    }

    public void removeSession(Channel channel) {
        String channelId = channel.id().asLongText();
        Long userId = channelUserMap.remove(channelId);
        if (userId != null) {
            userChannelMap.remove(userId);
            log.info("User {} disconnected, channel: {}", userId, channelId);
        }
    }

    public Channel getChannel(Long userId) {
        return userChannelMap.get(userId);
    }

    public Long getUserId(Channel channel) {
        return channelUserMap.get(channel.id().asLongText());
    }
}
