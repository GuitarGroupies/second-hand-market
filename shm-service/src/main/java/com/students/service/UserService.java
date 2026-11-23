package com.students.service;

import com.students.pojo.dto.UserLoginDTO;
import com.students.pojo.dto.UserRegisterDTO;
import com.students.pojo.vo.LoginVO;
import com.students.pojo.vo.UserVO;

/**
 * 用户业务接口。
 *
 * @author students
 */
public interface UserService {

    /**
     * 用户注册。
     *
     * @param dto 注册入参
     * @return 新用户 ID
     */
    Long register(UserRegisterDTO dto);

    /**
     * 用户登录。
     *
     * @param dto 登录入参
     * @return 登录结果（含 token 与用户信息）
     */
    LoginVO login(UserLoginDTO dto);

    /**
     * 退出登录，清除 Redis 中的 token。
     *
     * @param userId 当前登录用户 ID
     */
    void logout(Long userId);

    /**
     * 根据 ID 查询用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserVO getUserById(Long userId);
}
