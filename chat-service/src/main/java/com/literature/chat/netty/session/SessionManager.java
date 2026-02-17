package com.literature.chat.netty.session;

import com.literature.chat.util.UserKeyUtil;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
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
    public static final AttributeKey<Long> ATTR_USER_ID = AttributeKey.valueOf("userId");
    public static final AttributeKey<String> ATTR_USER_TYPE = AttributeKey.valueOf("userType");

    /**
     * userKey -> Channel (userKey = userType:userId)
     */
    private final Map<String, Channel> userChannelMap = new ConcurrentHashMap<>();

    /**
     * ChannelId -> userKey
     */
    private final Map<String, String> channelUserMap = new ConcurrentHashMap<>();

    public void addSession(Long userId, Channel channel) {
        addSession(userId, null, channel);
    }

    public void addSession(Long userId, String userType, Channel channel) {
        String userKey = UserKeyUtil.build(userId, userType);
        Channel oldChannel = userChannelMap.put(userKey, channel);
        if (oldChannel != null && oldChannel != channel) {
            channelUserMap.remove(oldChannel.id().asLongText());
            oldChannel.close();
            log.warn("用户 {} 重复连接，已关闭旧 Channel {}", userId, oldChannel.id());
        }
        channelUserMap.put(channel.id().asLongText(), userKey);
        channel.attr(ATTR_USER_ID).set(userId);
        if (userType != null) {
            channel.attr(ATTR_USER_TYPE).set(userType);
        }
        log.info("用户 {} 已连接, channel: {}", userId, channel.id());
    }

    /**
     * 移除会话映射，返回被移除的 userId（可能为 null）
     */
    public Long removeSession(Channel channel) {
        String channelId = channel.id().asLongText();
        String userKey = channelUserMap.remove(channelId);
        if (userKey != null) {
            userChannelMap.remove(userKey);
            log.info("用户 {} 已断开, channel: {}", userKey, channelId);
        }
        return channel.attr(ATTR_USER_ID).get();
    }

    public Channel getChannel(Long userId, String userType) {
        return userChannelMap.get(UserKeyUtil.build(userId, userType));
    }

    public Long getUserId(Channel channel) {
        return channel.attr(ATTR_USER_ID).get();
    }

    public String getUserType(Channel channel) {
        return channel.attr(ATTR_USER_TYPE).get();
    }
}
