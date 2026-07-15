package com.whut.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek API 自定义配置属性。
 * 配置前缀: edu.ai.deepseek
 * 这些属性是自定义的，供应用层读取和展示。
 * 实际的 API 调用参数由 spring.ai.openai.* 控制。
 */
@ConfigurationProperties(prefix = "edu.ai.deepseek")
public class DeepSeekProperties {

    /** DeepSeek API 地址 */
    private String baseUrl = "https://api.deepseek.com";

    /** API Key */
    private String apiKey = "";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 温度 */
    private Double temperature = 0.7;

    /** 最大 Token 数 */
    private Integer maxTokens = 2048;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
}
