package com.whut.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import com.whut.common.result.Result;
import com.whut.common.util.JwtUtil;
import com.whut.user.dto.LoginRequest;
import com.whut.user.dto.UserCreateRequest;
import com.whut.user.dto.UserUpdateRequest;
import com.whut.user.entity.User;
import com.whut.user.mapper.UserMapper;
import com.whut.user.vo.LoginResponse;
import com.whut.user.vo.PublicUserResponse;
import com.whut.user.vo.UserResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final JwtUtil jwtUtil;

    public UserService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Value("${edu.bootstrap.admin-username:admin}")
    private String adminUsername;

    @Value("${edu.bootstrap.admin-password:admin123}")
    private String adminPassword;

    @Value("${edu.bootstrap.admin-nickname:系统管理员}")
    private String adminNickname;

    @PostConstruct
    public void initAdmin() {
        long count = count();
        if (count > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPasswordHash(ENCODER.encode(adminPassword));
        admin.setNickname(adminNickname);
        admin.setRole(UserRole.ADMIN.getCode());
        save(admin);
    }

    public LoginResponse login(LoginRequest request) {
        User user = findByUsernameOrId(request.getUsername());
        if (user == null || !ENCODER.matches(request.getPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, toResponse(user));
    }

    public UserResponse register(UserCreateRequest request) {
        if (request.getRole() == null
                || (request.getRole() != UserRole.STUDENT.getCode() && request.getRole() != UserRole.TEACHER.getCode())) {
            throw BusinessException.badRequest("只能注册学生或教师账号");
        }
        return createUser(request, false);
    }

    public UserResponse currentUser() {
        AuthUser authUser = AuthContext.get();
        if (authUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return toResponse(getById(authUser.getId()));
    }

    /**
     * 带条件的分页查询用户列表
     *
     * @param page     页码，从 1 开始
     * @param size     每页条数
     * @param keyword  搜索关键词（匹配用户名或昵称）
     * @param role     角色过滤（null 表示不过滤）
     * @return 分页结果
     */
    public IPage<UserResponse> page(int page, int size, String keyword, Integer role) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .and(StringUtils.isNotBlank(keyword), w -> w
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getNickname, keyword))
                .eq(role != null, User::getRole, role)
                .orderByDesc(User::getId);

        long total = baseMapper.selectCount(wrapper);
        List<User> records = baseMapper.selectList(wrapper.last("limit " + safeSize + " offset " + ((safePage - 1) * safeSize)));

        com.baomidou.mybatisplus.core.metadata.IPage<UserResponse> result = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(safePage, safeSize, total);
        result.setRecords(records.stream().map(this::toResponse).toList());
        return result;
    }

    /**
     * 旧版分页（仅分页，无条件），保留兼容
     */
    public com.baomidou.mybatisplus.core.metadata.IPage<UserResponse> pageOld(int page, int size) {
        return page(page, size, null, null);
    }

    public UserResponse create(UserCreateRequest request) {
        return createUser(request, false);
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        if (StringUtils.isNotBlank(request.getPassword())) {
            user.setPasswordHash(ENCODER.encode(request.getPassword()));
        }
        if (StringUtils.isNotBlank(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        baseMapper.update(user);
        return toResponse(user);
    }

    public void delete(Long id) {
        User user = getById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        baseMapper.deleteById(id);
    }

    public PublicUserResponse getPublicUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return new PublicUserResponse(user.getId(), user.getNickname(), user.getRole());
    }

    // ---- internal helpers ----

    private UserResponse createUser(UserCreateRequest request, boolean isAdmin) {
        if (countByUsername(request.getUsername()) > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }
        assertValidRole(request.getRole());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(ENCODER.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole(request.getRole());
        save(user);
        return toResponse(user);
    }

    private User findByUsernameOrId(String input) {
        // 支持通过用户名或用户 ID 登录
        if (input != null && input.matches("\\d+")) {
            User byId = getById(Long.parseLong(input));
            if (byId != null) {
                return byId;
            }
        }
        return lambdaQuery()
                .eq(User::getUsername, input)
                .one();
    }

    private Long countByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .count();
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
        throw BusinessException.badRequest("无效的角色编码");
    }

    private UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setNickname(user.getNickname());
        r.setRole(user.getRole());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }

    public com.whut.user.vo.UserCountResponse countByRole() {
        long studentCount = lambdaQuery().eq(User::getRole, com.whut.common.enums.UserRole.STUDENT.getCode()).count();
        long teacherCount = lambdaQuery().eq(User::getRole, com.whut.common.enums.UserRole.TEACHER.getCode()).count();
        return new com.whut.user.vo.UserCountResponse(studentCount, teacherCount);
    }
}
