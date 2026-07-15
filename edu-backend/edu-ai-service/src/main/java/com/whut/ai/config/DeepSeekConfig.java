package com.whut.ai.config;

import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * DeepSeek API 配置。
 * DeepSeek 兼容 OpenAI API 格式，复用 Spring AI 的 OpenAI 客户端。
 */
@Configuration
@AutoConfigureBefore(OpenAiAutoConfiguration.class)
@EnableConfigurationProperties(DeepSeekProperties.class)
public class DeepSeekConfig {

    private final DeepSeekProperties deepSeekProperties;
    private final AiProperties aiProperties;

    public DeepSeekConfig(DeepSeekProperties deepSeekProperties, AiProperties aiProperties) {
        this.deepSeekProperties = deepSeekProperties;
        this.aiProperties = aiProperties;
    }

    @Bean
    @Primary
    public OpenAiApi deepSeekApi() {
        return OpenAiApi.builder()
                .baseUrl(deepSeekProperties.getBaseUrl())
                .apiKey(deepSeekProperties.getApiKey())
                .build();
    }

    @Bean
    @Primary
    public OpenAiChatModel deepSeekChatModel(OpenAiApi deepSeekApi) {
        return OpenAiChatModel.builder()
                .openAiApi(deepSeekApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(deepSeekProperties.getModel())
                        .temperature(deepSeekProperties.getTemperature())
                        .maxTokens(deepSeekProperties.getMaxTokens())
                        .build())
                .build();
    }
}
