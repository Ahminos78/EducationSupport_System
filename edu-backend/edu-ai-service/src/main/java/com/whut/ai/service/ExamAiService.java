package com.whut.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whut.ai.dto.ExamAiRequest;
import com.whut.ai.vo.ExamQuestionAiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamAiService {

    private static final Logger log = LoggerFactory.getLogger(ExamAiService.class);
    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    public ExamAiService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
    }

    public List<ExamQuestionAiResponse> generateQuestions(ExamAiRequest request) {
        String promptText = buildQuestionPrompt(request);
        String response = callModel(promptText);
        return parseQuestions(response);
    }

    public String autoComment(ExamAiRequest request) {
        String promptText = buildCommentPrompt(request);
        return callModel(promptText);
    }

    private String callModel(String promptText) {
        Prompt prompt = new Prompt(new UserMessage(promptText));
        var aiResponse = chatModel.call(prompt);
        var result = aiResponse.getResult();
        return result != null && result.getOutput() != null
                ? result.getOutput().getText().trim() : "";
    }

    private String buildQuestionPrompt(ExamAiRequest request) {
        String courseInfo = request.getCourseName();
        if (request.getCourseDescription() != null && !request.getCourseDescription().isBlank()) {
            courseInfo += " - " + request.getCourseDescription();
        }
        int count = Math.max(1, Math.min(request.getCount(), 50));
        return String.format("""
                你是一位计算机专业课程的资深出题专家。请为课程「%s」生成 %d 道考试题目。

                要求：
                1. 题目类型包括选择题和填空题，混合出题
                2. 选择题包含4个选项（A/B/C/D），标注正确答案字母
                3. 填空题答案简洁准确
                4. 题目难度适中，覆盖课程核心知识点
                5. 每道题10分

                请严格按照以下JSON数组格式输出（不要包含其他内容）：
                [
                  {
                    "type": 0,
                    "title": "题目标题",
                    "options": ["选项A", "选项B", "选项C", "选项D"],
                    "answer": "A"
                  },
                  {
                    "type": 1,
                    "title": "填空题题目",
                    "options": [],
                    "answer": "正确答案"
                  }
                ]
                """, courseInfo, count);
    }

    private String buildCommentPrompt(ExamAiRequest request) {
        String title = request.getAssignmentTitle() != null ? request.getAssignmentTitle() : "作业";
        String content = request.getSubmissionContent() != null ? request.getSubmissionContent() : "无内容";
        Integer score = request.getScore();
        String scoreInfo = score != null ? "该学生得分：" + score + "分。" : "";
        return String.format("""
                你是一位计算机课程的助教，请根据以下作业信息和学生提交内容，生成一段简短的评价（50-150字）。

                作业题目：%s
                学生提交内容：%s
                %s

                要求：
                1. 指出学生的优点和不足
                2. 给出具体的改进建议
                3. 语气友善、鼓励性
                4. 控制在150字以内
                """, title, content, scoreInfo);
    }

    List<ExamQuestionAiResponse> parseQuestions(String aiText) {
        if (aiText == null || aiText.isBlank()) return List.of();

        try {
            int start = aiText.indexOf('[');
            int end = aiText.lastIndexOf(']');
            if (start < 0 || end <= start) return List.of();
            String json = aiText.substring(start, end + 1);

            List<ExamQuestionAiResponse> raw = objectMapper.readValue(json,
                    new TypeReference<List<ExamQuestionAiResponse>>() {});

            List<ExamQuestionAiResponse> valid = raw.stream()
                    .filter(q -> q.getTitle() != null && !q.getTitle().isBlank())
                    .collect(Collectors.toList());

            for (ExamQuestionAiResponse q : valid) {
                if (q.getOptions() == null) q.setOptions("[]");
                if (q.getAnswer() == null) q.setAnswer("");
                if (q.getScore() == null || q.getScore() <= 0) q.setScore(10);
            }

            if (valid.size() < raw.size()) {
                log.warn("过滤了 {} 道空题目, AI原始返回 {} 道", raw.size() - valid.size(), raw.size());
            }
            return valid;
        } catch (Exception e) {
            log.warn("解析AI生成的题目失败, 返回空列表: {}", e.getMessage());
            return List.of();
        }
    }
}