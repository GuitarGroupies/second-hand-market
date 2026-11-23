package com.students.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类，负责生成与解析 Token。
 *
 * @author students
 */
@Component
public class JwtUtil {

    /** 签名密钥，从配置读取，长度至少 32 字节（HS256 要求） */
    private final SecretKey secretKey;

    /** Token 有效期，单位秒 */
    private final long expireSeconds;

    public JwtUtil(@Value("${shm.jwt.secret}") String secret,
                   @Value("${shm.jwt.expire-seconds}") long expireSeconds) {
        // HS256 要求密钥不少于 256 位（32 字节）
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
    }

    /**
     * 生成 Token。
     *
     * @param userId 用户 ID，会写入载荷
     * @return JWT 字符串
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 Token，取出用户 ID。
     *
     * @param token JWT 字符串
     * @return 用户 ID；token 无效、过期或被篡改时返回 null
     */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            // 签名不对、token 过期、格式错误都走这里
            return null;
        }
    }
}