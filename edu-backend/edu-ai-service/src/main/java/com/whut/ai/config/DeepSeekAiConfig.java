package com.whut.ai.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DeepSeekAiConfig {

    @Value("${siliconflow.api-key}")
    private String siliconFlowApiKey;

    @Value("${siliconflow.base-url:https://api.siliconflow.cn/v1}")
    private String siliconFlowBaseUrl;

    @Value("${siliconflow.embedding.model:BAAI/bge-large-zh-v1.5}")
    private String siliconFlowModel;

    @Bean
    @Primary
    public EmbeddingModel siliconFlowEmbeddingModel() {
        System.out.println("=== 初始化 SiliconFlow Embedding模型: " + siliconFlowModel + " ===");
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(siliconFlowBaseUrl)
                .apiKey(siliconFlowApiKey)
                .build();
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(siliconFlowModel)
                .build();
        return new OpenAiEmbeddingModel(api, MetadataMode.EMBED, options);
    }
}
