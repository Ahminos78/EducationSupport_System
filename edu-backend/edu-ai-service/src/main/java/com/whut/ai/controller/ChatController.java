package com.whut.ai.controller;

import com.whut.ai.dto.ChatRequest;
import com.whut.ai.dto.ChatResponse;
import com.whut.ai.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        log.info("收到聊天请求 | 问题: {}", request.getQuestion());
        try {
            ChatResponse response = chatService.chat(request);
            return ResponseEntity.ok(Map.of(
                    "success", response.isSuccess(),
                    "answer", response.getAnswer() != null ? response.getAnswer() : "",
                    "sources", response.getSources() != null ? response.getSources() : java.util.List.of(),
                    "model", response.getModel() != null ? response.getModel() : ""
            ));
        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "edu-ai-service"
        ));
    }
}
