package com.whut.ai.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 嵌入模型配置类
 * 用 SiliconFlow 的 BAAI/bge-large-zh-v1.5 模型做向量化，
 * 解决 DeepSeek 不支持 embeddings 接口的问题。
 *
 * 分工：
 * - DeepSeek → 负责 Chat（大模型对话）
 * - SiliconFlow → 负责 Embedding（文本向量化，供 RAG 检索用）
 * - Chroma → 负责向量存储
 */
@Configuration
public class SiliconFlowEmbeddingConfig {

    @Value("${siliconflow.base-url}")
    private String siliconflowBaseUrl;

    @Value("${siliconflow.api-key}")
    private String siliconflowApiKey;

    @Value("${siliconflow.embedding.model}")
    private String embeddingModel;

    @Bean
    @Primary
    public OpenAiEmbeddingModel siliconFlowEmbeddingModel() {
        // 自定义 Jackson 消息转换器，支持各种 Content-Type
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>(jsonConverter.getSupportedMediaTypes());
        mediaTypes.add(MediaType.parseMediaType("application/octet-stream"));
        jsonConverter.setSupportedMediaTypes(mediaTypes);

        // 构建 RestClient，指向 SiliconFlow API
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl(siliconflowBaseUrl)
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    converters.add(jsonConverter);
                });

        // 创建 OpenAiApi（SiliconFlow 兼容 OpenAI 格式）
        OpenAiApi openAiApi = new OpenAiApi(
                siliconflowBaseUrl,
                siliconflowApiKey,
                "/v1/chat/completions",
                "/v1/embeddings",
                restClientBuilder,
                WebClient.builder(),
                RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER
        );

        // 创建 EmbeddingModel
        return new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model(embeddingModel)
                        .build()
        );
    }
}
