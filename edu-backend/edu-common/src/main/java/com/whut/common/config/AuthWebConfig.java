package com.whut.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthWebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final RoleInterceptor roleInterceptor;
    private final AuthProperties authProperties;

    public AuthWebConfig(AuthInterceptor authInterceptor,
                         RoleInterceptor roleInterceptor,
                         AuthProperties authProperties) {
        this.authInterceptor = authInterceptor;
        this.roleInterceptor = roleInterceptor;
        this.authProperties = authProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] pathPatterns = authProperties.getPathPatterns().toArray(String[]::new);
        String[] excludePaths = authProperties.getExcludePaths().toArray(String[]::new);
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(pathPatterns)
                .excludePathPatterns(excludePaths);
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns(pathPatterns);
    }
}
