package com.whut.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import com.whut.common.util.JwtUtil;
import com.whut.user.dto.*;
import com.whut.user.entity.User;
import com.whut.user.mapper.UserMapper;
import com.whut.user.vo.LoginResponse;
import com.whut.user.vo.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    private UserService userService;
    private User testUser;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserService(jwtUtil);
        // Inject baseMapper via reflection to support parent class methods (getById, save, etc.)
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(userService, userMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash(encoder.encode("password123"));
        testUser.setNickname("\u6d4b\u8bd5\u7528\u6237");
        testUser.setRole(UserRole.STUDENT.getCode());
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
    }

    // ==================== register() ====================

    @Test
    void register_shouldSucceed() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("pass123");
        request.setNickname("\u65b0\u7528\u6237");
        request.setRole(UserRole.STUDENT.getCode());

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any())).thenReturn(1);

        UserResponse response = userService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getNickname()).isEqualTo("\u65b0\u7528\u6237");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existing");
        request.setPassword("pass123");
        request.setNickname("existingUser");
        request.setRole(UserRole.STUDENT.getCode());

        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u7528\u6237\u540d\u5df2\u5b58\u5728");
    }

    // ==================== login() ====================

    @Test
    void login_shouldSucceed() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(jwtUtil.generateToken(1L, "testuser", UserRole.STUDENT.getCode()))
                .thenReturn("mock.jwt.token");

        LoginResponse response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void login_shouldThrowWhenWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef");
    }

    // ==================== forgotPasswordVerify() ====================

    @Test
    void forgotPasswordVerify_shouldSucceedByEmail() {
        ForgotPasswordVerifyRequest request = new ForgotPasswordVerifyRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(jwtUtil.generateResetToken(1L)).thenReturn("reset-token");

        String token = userService.forgotPasswordVerify(request);

        assertThat(token).isEqualTo("reset-token");
    }

    @Test
    void forgotPasswordVerify_shouldThrowWhenMismatch() {
        ForgotPasswordVerifyRequest request = new ForgotPasswordVerifyRequest();
        request.setUsername("testuser");
        request.setEmail("wrong@example.com");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        assertThatThrownBy(() -> userService.forgotPasswordVerify(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u90ae\u7bb1\u6216\u624b\u673a\u53f7\u4e0d\u5339\u914d");
    }

    // ==================== forgotPasswordReset() ====================

    @Test
    void forgotPasswordReset_shouldSucceed() {
        ForgotPasswordResetRequest request = new ForgotPasswordResetRequest();
        request.setToken("valid-token");
        request.setNewPassword("newpass123");

        when(jwtUtil.parseResetToken("valid-token")).thenReturn(1L);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        userService.forgotPasswordReset(request);

        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void forgotPasswordReset_shouldThrowWhenUserNotFound() {
        ForgotPasswordResetRequest request = new ForgotPasswordResetRequest();
        request.setToken("valid-token");
        request.setNewPassword("newpass123");

        when(jwtUtil.parseResetToken("valid-token")).thenReturn(99L);
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userService.forgotPasswordReset(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u7528\u6237\u4e0d\u5b58\u5728");
    }

    // ==================== changePassword() using static AuthContext ====================

    @Test
    void changePassword_shouldSucceed() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("password123");
        request.setNewPassword("newpass456");

        AuthUser authUser = new AuthUser(1L, "testuser", UserRole.STUDENT.getCode());

        try (MockedStatic<AuthContext> authCtx = mockStatic(AuthContext.class)) {
            authCtx.when(AuthContext::get).thenReturn(authUser);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            userService.changePassword(request);

            verify(userMapper).updateById(any(User.class));
        }
    }

    @Test
    void changePassword_shouldThrowWhenWrongOldPassword() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("wrongoldpassword");
        request.setNewPassword("newpass456");

        AuthUser authUser = new AuthUser(1L, "testuser", UserRole.STUDENT.getCode());

        try (MockedStatic<AuthContext> authCtx = mockStatic(AuthContext.class)) {
            authCtx.when(AuthContext::get).thenReturn(authUser);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("\u65e7\u5bc6\u7801\u9519\u8bef");
        }
    }
}
