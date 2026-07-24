package com.whut.assessment.client;

import com.whut.assessment.dto.AiAutoCommentRequest;
import com.whut.assessment.dto.AiGenerateQuestionsRequest;
import com.whut.assessment.vo.QuestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class AiExamClient {

    private static final Logger log = LoggerFactory.getLogger(AiExamClient.class);

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public AiExamClient(RestTemplate restTemplate,
                        @Value("${edu.ai-service.url:http://127.0.0.1:8060}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    public List<QuestionResponse> generateQuestions(AiGenerateQuestionsRequest request) {
        try {
            String url = aiServiceUrl + "/api/ai/exam/generate-questions";
            HttpEntity<AiGenerateQuestionsRequest> entity = new HttpEntity<>(request);
            var response = restTemplate.postForEntity(url, entity, GenericResult.class);
            var body = response.getBody();
            if (body != null && body.code == 200 && body.data != null) {
                return body.data;
            }
        } catch (Exception e) {
            log.warn("调用AI服务生成题目失败, 回退到模板: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public String autoComment(AiAutoCommentRequest request) {
        try {
            String url = aiServiceUrl + "/api/ai/exam/auto-comment";
            HttpEntity<AiAutoCommentRequest> entity = new HttpEntity<>(request);
            var response = restTemplate.postForEntity(url, entity, GenericStringResult.class);
            var body = response.getBody();
            if (body != null && body.code == 200) {
                return body.data;
            }
        } catch (Exception e) {
            log.warn("调用AI服务生成评语失败: {}", e.getMessage());
        }
        return "";
    }

    private static class GenericResult {
        public int code;
        public String message;
        public List<QuestionResponse> data;
    }

    private static class GenericStringResult {
        public int code;
        public String message;
        public String data;
    }
}