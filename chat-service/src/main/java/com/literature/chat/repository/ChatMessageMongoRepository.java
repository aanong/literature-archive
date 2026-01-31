package com.literature.chat.repository;

import com.literature.chat.entity.ChatMessageDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDoc, String> {

    /**
     * 查询会话历史消息
     */
    Page<ChatMessageDoc> findBySessionIdOrderByTimestampDesc(Long sessionId, Pageable pageable);

    /**
     * 查询离线消息 (时间戳大于 lastAckTime)
     */
    List<ChatMessageDoc> findBySessionIdAndTimestampGreaterThan(Long sessionId, Long lastAckTime);

    /**
     * 查询用户作为接收者的离线单聊消息
     */
    List<ChatMessageDoc> findByTargetUserIdAndTimestampGreaterThan(Long targetUserId, Long lastAckTime);
}
