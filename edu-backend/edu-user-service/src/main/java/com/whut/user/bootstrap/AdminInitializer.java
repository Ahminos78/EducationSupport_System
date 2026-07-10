package com.whut.user.bootstrap;

import com.whut.common.enums.UserRole;
import com.whut.user.dto.UserCreateRequest;
import com.whut.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserService userService;

    @Value("${edu.bootstrap.admin-username:admin}")
    private String adminUsername;

    @Value("${edu.bootstrap.admin-password:admin123}")
    private String adminPassword;

    @Value("${edu.bootstrap.admin-nickname:系统管理员}")
    private String adminNickname;

    public AdminInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        if (userService.count() > 0) {
            return;
        }
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(adminUsername);
        request.setPassword(adminPassword);
        request.setNickname(adminNickname);
        request.setRole(UserRole.ADMIN.getCode());
        userService.create(request);
    }
}
