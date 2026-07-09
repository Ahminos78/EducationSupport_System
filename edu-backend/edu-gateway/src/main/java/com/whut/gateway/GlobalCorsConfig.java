package com.whut.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 跨域解决配置类
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许访问的来源域名，* 表示允许任何源（生产环境请修改）
        config.addAllowedOriginPattern("*");

        // 放行的请求头
        config.addAllowedHeader("*");

        // 放行的请求方式：GET, POST, PUT, DELETE, OPTIONS
        config.addAllowedMethod("*");

        // 暴露头部信息
        config.addExposedHeader("*");

        // 是否允许发送 Cookie
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
