package com.students.controller;

import com.students.common.context.UserContext;
import com.students.common.exception.BizException;
import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import com.students.pojo.dto.UserLoginDTO;
import com.students.pojo.dto.UserRegisterDTO;
import com.students.pojo.vo.LoginVO;
import com.students.pojo.vo.UserVO;
import com.students.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。
 *
 * @author students
 */
@Tag(name = "用户模块")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册。
     *
     * @param dto 注册入参
     * @return 新用户 ID
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody UserRegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    /**
     * 用户登录。
     *
     * @param dto 登录入参
     * @return token 与用户信息
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 获取当前登录用户信息。
     *
     * <p>本接口要求已登录：拦截器只负责解析 token，未登录时 userId 为 null，
     * 由这里主动抛出未登录异常。</p>
     *
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 退出登录。
     *
     * <p>删除 Redis 中的 token，使当前 token 立即失效。</p>
     *
     * @return 空结果
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        userService.logout(userId);
        return Result.success();
    }
}
