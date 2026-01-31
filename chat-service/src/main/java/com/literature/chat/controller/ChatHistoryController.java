package com.literature.chat.controller;

import com.literature.chat.entity.ChatMessageDoc;
import com.literature.chat.repository.ChatMessageMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/history")
public class ChatHistoryController {

    @Autowired
    private ChatMessageMongoRepository messageRepository;

    @GetMapping("/session")
    public ResponseEntity<Page<ChatMessageDoc>> getSessionHistory(
            @RequestParam Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatMessageDoc> history = messageRepository.findBySessionIdOrderByTimestampDesc(sessionId, pageRequest);
        return ResponseEntity.ok(history);
    }
}
