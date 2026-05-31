package com.edulink.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT（JSON Web Token）工具类
 * 负责生成和解析JWT令牌，用于用户身份认证和授权
 */
@Component
public class JwtUtil {
    // 密钥长度需要 >= 256 bits (32 bytes)，用于HMAC-SHA256签名
    private static final String SECRET = "edulink-secret-key-2024-blog-project-springboot-vue-32bytes";
    private static final long EXPIRE = 7 * 24 * 60 * 60 * 1000; // 令牌有效期：7天（单位：毫秒）

    /**
     * 获取签名密钥（HMAC-SHA256）
     *
     * @return Key 对象，用于签名和验签
     */
    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     * 生成JWT令牌
     *
     * @param userId   用户ID（存为subject）
     * @param username 用户名（作为自定义claim）
     * @param role     用户角色（如ADMIN、TEACHER、STUDENT、PARENT）
     * @return 生成的JWT字符串
     */
    public static String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRE);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))               // 设置主题（用户ID）
                .claim("username", username)                      // 添加自定义声明：用户名
                .claim("role", role)                              // 添加自定义声明：角色
                .setIssuedAt(now)                                 // 签发时间
                .setExpiration(expiration)                        // 过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 使用HS256算法签名
                .compact();
    }

    /**
     * 解析JWT令牌，获取Claims（声明体）
     *
     * @param token JWT字符串（可能包含Bearer前缀）
     * @return Claims对象，解析失败则返回null
     */
    public static Claims parseToken(String token) {
        try {
            // 去除Bearer前缀（如果存在）
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())               // 设置验签密钥
                    .build()
                    .parseClaimsJws(token)                        // 解析并验证签名
                    .getBody();
        } catch (Exception e) {
            // 任何解析异常（过期、签名错误、格式错误）均返回null
            return null;
        }
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT字符串
     * @return 用户ID，解析失败返回null
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? Long.parseLong(claims.getSubject()) : null;
    }

    /**
     * 从JWT令牌中获取用户名
     *
     * @param token JWT字符串
     * @return 用户名，解析失败返回null
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从JWT令牌中获取用户角色
     *
     * @param token JWT字符串
     * @return 角色（如ADMIN、TEACHER、STUDENT、PARENT），解析失败返回null
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", String.class) : null;
    }
}