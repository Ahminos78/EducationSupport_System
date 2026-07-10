package com.whut.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.whut.common.annotation.RequireRole;
import com.whut.common.enums.UserRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.whut.common.result.Result;
import com.whut.user.dto.LoginRequest;
import com.whut.user.dto.UserCreateRequest;
import com.whut.user.dto.UserUpdateRequest;
import com.whut.user.service.UserService;
import com.whut.user.vo.LoginResponse;
import com.whut.user.vo.PublicUserResponse;
import com.whut.user.vo.UserResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<UserResponse> register(@RequestBody UserCreateRequest request) {
        return Result.success(userService.register(request));
    }

    @GetMapping("/me")
    public Result<UserResponse> me() {
        return Result.success(userService.currentUser());
    }

    @GetMapping("/{id}/public")
    public Result<PublicUserResponse> publicUser(@PathVariable Long id) {
        return Result.success(userService.getPublicUser(id));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/page")
    public Result<IPage<UserResponse>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role) {
        return Result.success(userService.page(page, size, keyword, role));
    }

    @RequireRole(UserRole.ADMIN)
    @PostMapping
    public Result<UserResponse> create(@RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @RequireRole(UserRole.ADMIN)
    @PutMapping("/{id}")
    public Result<UserResponse> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return Result.success(userService.update(id, request));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
