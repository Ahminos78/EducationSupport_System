package com.whut.ai.controller;

import com.whut.ai.dto.ChatRequest;
import com.whut.ai.dto.ExamAiRequest;
import com.whut.ai.service.ChatService;
import com.whut.ai.service.ExamAiService;
import com.whut.ai.vo.ChatResponse;
import com.whut.ai.vo.ExamQuestionAiResponse;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatService chatService;
    private final ExamAiService examAiService;

    public ChatController(ChatService chatService, ExamAiService examAiService) {
        this.chatService = chatService;
        this.examAiService = examAiService;
    }

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        return Result.success(chatService.chat(request));
    }

    @DeleteMapping("/chat/session")
    public Result<Void> clearSession(@RequestParam String sessionId) {
        chatService.clearSession(sessionId);
        return Result.success();
    }

    @PostMapping("/exam/generate-questions")
    public Result<List<ExamQuestionAiResponse>> generateQuestions(@RequestBody ExamAiRequest request) {
        return Result.success(examAiService.generateQuestions(request));
    }

    @PostMapping("/exam/auto-comment")
    public Result<String> autoComment(@RequestBody ExamAiRequest request) {
        return Result.success(examAiService.autoComment(request));
    }
}
