package com.whut.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.whut.ai.config.DeepSeekProperties;
import com.whut.ai.config.AiProperties;

@SpringBootApplication
@ComponentScan(basePackages = {"com.whut.ai", "com.whut.common"})
@EnableConfigurationProperties({DeepSeekProperties.class, AiProperties.class})
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
