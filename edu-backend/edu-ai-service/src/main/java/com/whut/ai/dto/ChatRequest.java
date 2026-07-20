package com.whut.ai.dto;

import java.util.List;

public class ChatRequest {

    /** 消息内容 */
    private String message;

    /** 会话 ID（用于延续上下文，为空则新建会话） */
    private String sessionId;

    /** 模型名称（为空则使用默认模型） */
    private String model;

    /** 系统提示词（可选，覆盖默认） */
    private String systemPrompt;

    /** 温度参数（可选，覆盖默认） */
    private Double temperature;

    /** 是否启用 RAG 增强 */
    private Boolean useRag;

    /** 关联的课程 ID（用于限定 RAG 检索范围） */
    private Long courseId;

    /** 文件附件（支持文件路径或文件ID） */
    private List<String> attachments;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Boolean getUseRag() {
        return useRag;
    }

    public void setUseRag(Boolean useRag) {
        this.useRag = useRag;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
}
