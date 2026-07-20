package com.whut.ai.service;

import com.whut.ai.dto.ExamGenerateRequest;
import com.whut.ai.rag.RagService;
import com.whut.ai.vo.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExamQuestionService {

    private static final Logger log = LoggerFactory.getLogger(ExamQuestionService.class);

    private final OpenAiChatModel chatModel;

    @Autowired(required = false)
    private RagService ragService;

    public ExamQuestionService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public List<Map<String, Object>> generate(ExamGenerateRequest request) {
        int count = request.getCount() != null ? request.getCount() : 10;
        String courseContext = request.getKnowledgeContext();

        if (courseContext == null && ragService != null && request.getCourseId() != null) {
            courseContext = ragService.retrieveContext(request.getCourseName(), request.getCourseId());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("你是一位高校计算机专业的资深出题教师。");
        sb.append("请为课程「").append(request.getCourseName()).append("」生成").append(count).append("道考试题目。\n\n");
        sb.append("### 格式要求\n");
        sb.append("请严格按照以下JSON数组格式返回，不要包含任何其他文字说明：\n");
        sb.append("[{\"type\":0,\"title\":\"题目内容\",\"options\":[\"A选项\",\"B选项\",\"C选项\",\"D选项\"],\"answer\":\"A\",\"score\":10}]\n\n");
        sb.append("### 规则\n");
        sb.append("- type=0表示选择题，type=1表示填空题\n");
        sb.append("- 选择题必须有4个选项，answer填正确选项字母\n");
        sb.append("- 填空题options为null，answer填正确答案\n");
        sb.append("- 每道题score为10分\n");
        sb.append("- 题目应覆盖课程核心知识点，难度适中\n");

        if (courseContext != null && !courseContext.isBlank()) {
            sb.append("\n### 参考资料\n");
            sb.append("请参考以下课程资料出题：\n").append(courseContext);
        }

        UserMessage userMessage = new UserMessage(sb.toString());
        Prompt prompt = new Prompt(List.of(
                new SystemMessage("你是一个专业的考试出题助手。请严格按照JSON格式返回题目数据。"),
                userMessage
        ));

        try {
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            String content = aiResponse.getResult().getOutput().getText();
            return parseQuestions(content, count);
        } catch (Exception e) {
            log.error("AI出题失败", e);
            throw new RuntimeException("AI出题失败，请稍后重试");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseQuestions(String content, int count) {
        try {
            int start = content.indexOf('[');
            int end = content.lastIndexOf(']');
            if (start >= 0 && end > start) {
                content = content.substring(start, end + 1);
            }
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> questions = mapper.readValue(content, List.class);
            if (questions.size() > count) {
                questions = questions.subList(0, count);
            }
            return questions;
        } catch (Exception e) {
            log.warn("解析AI出题结果失败，返回空列表", e);
            return new ArrayList<>();
        }
    }
}
