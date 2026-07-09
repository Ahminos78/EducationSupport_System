package com.whut.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import com.whut.common.util.JwtUtil;
import com.whut.user.dto.LoginRequest;
import com.whut.user.dto.UserCreateRequest;
import com.whut.user.dto.UserUpdateRequest;
import com.whut.user.entity.User;
import com.whut.user.mapper.UserMapper;
import com.whut.user.vo.LoginResponse;
import com.whut.user.vo.PublicUserResponse;
import com.whut.user.vo.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        requireText(request.getUsername(), "用户名不能为空");
        requireText(request.getPassword(), "密码不能为空");
        User user = findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, toResponse(user));
    }

    public UserResponse currentUser() {
        AuthUser authUser = AuthContext.get();
        if (authUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return getById(authUser.getId());
    }

    public List<UserResponse> page(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return userMapper.findPage((safePage - 1) * safeSize, safeSize).stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse create(UserCreateRequest request) {
        requireText(request.getUsername(), "用户名不能为空");
        requireText(request.getPassword(), "密码不能为空");
        requireText(request.getNickname(), "昵称不能为空");
        assertValidRole(request.getRole());
        if (countByUsername(request.getUsername()) > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole(request.getRole());
        userMapper.insert(user);
        return getById(user.getId());
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findExistingById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getRole() != null) {
            assertValidRole(request.getRole());
            user.setRole(request.getRole());
        }
        userMapper.update(user);
        return getById(id);
    }

    public void delete(Long id) {
        if (userMapper.deleteById(id) == 0) {
            throw BusinessException.notFound("用户不存在");
        }
    }

    public UserResponse getById(Long id) {
        User user = findExistingById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return toResponse(user);
    }

    public PublicUserResponse getPublicUser(Long id) {
        User user = findExistingById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return new PublicUserResponse(user.getId(), user.getNickname(), user.getRole());
    }

    public long countAllUsers() {
        return userMapper.selectCount(new LambdaQueryWrapper<User>());
    }

    private User findExistingById(Long id) {
        return userMapper.selectById(id);
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    private Long countByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private void assertValidRole(Integer role) {
        if (role == null) {
            throw BusinessException.badRequest("角色不能为空");
        }
        for (UserRole userRole : UserRole.values()) {
            if (userRole.getCode() == role) {
                return;
            }
        }
        throw BusinessException.badRequest("角色编码不合法");
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw BusinessException.badRequest(message);
        }
    }
}
