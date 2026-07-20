package com.whut.ai.controller;

import com.whut.ai.dto.ChatRequest;
import com.whut.ai.service.ChatService;
import com.whut.ai.vo.ChatResponse;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 对话接口。
 * 提供与 AI 助手进行对话的能力，支持 RAG 增强。
 */
@RestController
@RequestMapping("/api/ai/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送对话消息
     */
    @PostMapping
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        return Result.success(chatService.chat(request));
    }

    /**
     * 清除会话历史
     */
    @DeleteMapping("/session")
    public Result<Void> clearSession(@RequestParam String sessionId) {
        chatService.clearSession(sessionId);
        return Result.success();
    }
}
