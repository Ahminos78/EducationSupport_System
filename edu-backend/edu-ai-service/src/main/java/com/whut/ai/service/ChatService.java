package com.whut.ai.service;

import com.whut.ai.config.AiProperties;
import com.whut.ai.dto.ChatRequest;
import com.whut.ai.rag.RagService;
import com.whut.ai.vo.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 对话服务。
 * 管理会话上下文，调用大模型（DeepSeek）进行对话。
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final OpenAiChatModel chatModel;
    private final AiProperties aiProperties;

    /** RAG 服务（可选注入：没有向量数据库时也能正常对话） */
    @Autowired(required = false)
    private RagService ragService;

    /** 简易会话历史存储（生产环境应替换为 Redis） */
    private final ConcurrentHashMap<String, List<Message>> sessions = new ConcurrentHashMap<>();

    public ChatService(OpenAiChatModel chatModel, AiProperties aiProperties) {
        this.chatModel = chatModel;
        this.aiProperties = aiProperties;
    }

    /**
     * 发送对话消息
     */
    public ChatResponse chat(ChatRequest request) {
        String sessionId = resolveSessionId(request);
        String model = request.getModel() != null ? request.getModel() : aiProperties.getDefaultModel();
        boolean useRag = request.getUseRag() != null ? request.getUseRag() : aiProperties.getRag().isEnabled();

        // 构建消息列表
        List<Message> messages = new ArrayList<>();

        // 系统提示词
        String systemPrompt = request.getSystemPrompt() != null
                ? request.getSystemPrompt() : aiProperties.getSystemPrompt();
        messages.add(new SystemMessage(systemPrompt));

        // 历史消息
        List<Message> history = sessions.get(sessionId);
        if (history != null) {
            messages.addAll(history);
        }

        // RAG 检索增强
        String userMessage = request.getMessage();
        if (useRag && ragService != null && aiProperties.getRag().isEnabled()) {
            String context = ragService.retrieveContext(request.getMessage(), request.getCourseId());
            if (!context.isBlank()) {
                userMessage = request.getMessage() + "\n\n" + context;
            }
        }
        messages.add(new UserMessage(userMessage));

        // 调用大模型
        Prompt prompt = new Prompt(messages);
        org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);

        // 提取结果
        Generation result = aiResponse.getResult();
        String content = result != null && result.getOutput() != null
                ? result.getOutput().getText() : "";

        // 保存历史
        saveHistory(sessionId, new UserMessage(userMessage),
                new AssistantMessage(content));

        // 构建响应
        ChatResponse response = buildResponse(content, sessionId, model);

        log.info("AI 对话完成: sessionId={}, model={}", sessionId, model);

        return response;
    }

    /**
     * 清除会话历史
     */
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        log.info("会话已清除: sessionId={}", sessionId);
    }

    private String resolveSessionId(ChatRequest request) {
        return request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
    }

    private void saveHistory(String sessionId, Message... newMessages) {
        sessions.compute(sessionId, (key, existing) -> {
            List<Message> list = existing != null ? existing : new ArrayList<>();
            list.addAll(List.of(newMessages));
            // 最多保留 20 条历史消息
            if (list.size() > 20) {
                return list.subList(list.size() - 20, list.size());
            }
            return list;
        });
    }

    private ChatResponse buildResponse(String content, String sessionId, String model) {
        ChatResponse response = new ChatResponse();
        response.setContent(content);
        response.setSessionId(sessionId);
        response.setModel(model);
        return response;
    }
}
