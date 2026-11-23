package com.students.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.students.common.constant.RedisKeyConstant;
import com.students.common.exception.BizException;
import com.students.common.result.ResultCode;
import com.students.common.util.JwtUtil;
import com.students.mapper.SysUserMapper;
import com.students.pojo.dto.UserLoginDTO;
import com.students.pojo.dto.UserRegisterDTO;
import com.students.pojo.entity.SysUser;
import com.students.pojo.vo.LoginVO;
import com.students.pojo.vo.UserVO;
import com.students.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 用户业务实现。
 *
 * @author students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 默认信用分 */
    private static final int DEFAULT_CREDIT_SCORE = 100;

    /** 用户状态：正常 */
    private static final int STATUS_NORMAL = 1;

    /** 用户状态：禁用 */
    private static final int STATUS_DISABLED = 0;

    private final SysUserMapper sysUserMapper;

    private final JwtUtil jwtUtil;

    private final StringRedisTemplate stringRedisTemplate;

    /** 密码加密器，线程安全，可复用 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Long register(UserRegisterDTO dto) {
        // 1. 查询用户名是否已存在
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 构建实体并加密密码
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                ? dto.getUsername() : dto.getNickname());
        user.setPhone(dto.getPhone() == null ? "" : dto.getPhone());
        user.setAvatar("");
        user.setCreditScore(DEFAULT_CREDIT_SCORE);
        user.setStatus(STATUS_NORMAL);

        // 3. 插入（createTime / updateTime / deleted 由 MyMetaObjectHandler 自动填充）
        sysUserMapper.insert(user);
        log.info("用户注册成功：username={}, userId={}", user.getUsername(), user.getId());
        return user.getId();
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        // 1. 查用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername()));
        // 用户不存在与密码错误使用同一提示，避免被枚举用户名
        if (user == null) {
            log.warn("登录失败：用户不存在，username={}", dto.getUsername());
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        // 2. 校验密码：matches(明文, 密文)，顺序不可颠倒
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("登录失败：密码错误，username={}", dto.getUsername());
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        // 3. 检查账号状态
        if (user.getStatus() != null && user.getStatus() == STATUS_DISABLED) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用，请联系客服");
        }

        // 4. 生成 Token
        String token = jwtUtil.generateToken(user.getId());

        // 5. 存 Redis，key 按 userId 组织，TTL 与 JWT 有效期一致
        String redisKey = RedisKeyConstant.AUTH_TOKEN + user.getId();
        stringRedisTemplate.opsForValue().set(
                redisKey, token, Duration.ofSeconds(RedisKeyConstant.AUTH_TOKEN_TTL_SECONDS));
        log.info("登录成功：username={}, userId={}, redisKey={}",
                user.getUsername(), user.getId(), redisKey);

        // 6. 组装返回
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(convertToUserVO(user));
        return loginVO;
    }

    @Override
    public void logout(Long userId) {
        String redisKey = RedisKeyConstant.AUTH_TOKEN + userId;
        stringRedisTemplate.delete(redisKey);
        log.info("用户退出登录：userId={}，已删除 Redis key={}", userId, redisKey);
    }

    @Override
    public UserVO getUserById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return convertToUserVO(user);
    }

    /**
     * 实体转 VO，只暴露前端需要的字段，绝不返回 password。
     *
     * @param entity 用户实体
     * @return 用户信息
     */
    private UserVO convertToUserVO(SysUser entity) {
        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setAvatar(entity.getAvatar());
        vo.setCreditScore(entity.getCreditScore());
        return vo;
    }
}
