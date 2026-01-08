package com.roominspection.backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

/**
 * JWT Token工具类
 * 用于IAM数据同步接口鉴权
 */
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 生成JWT Token
     *
     * @param appId     应用ID
     * @param secretKey 密钥
     * @return JWT Token
     */
    public static String generateToken(String appId, String secretKey) {
        return generateToken(appId, secretKey, null);
    }

    /**
     * 生成JWT Token（指定JWT ID）
     *
     * @param appId     应用ID
     * @param secretKey 密钥
     * @param jwtId     JWT ID（可选）
     * @return JWT Token
     */
    public static String generateToken(String appId, String secretKey, String jwtId) {
        String actualJwtId = jwtId != null ? jwtId : UUID.randomUUID().toString();

        return JWT.create()
                .withIssuer(appId)
                .withIssuedAt(new Date())
                .withJWTId(actualJwtId)
                .sign(Algorithm.HMAC256(secretKey));
    }

    /**
     * 验证JWT Token（默认60秒时间偏差）
     *
     * @param token     Token
     * @param secretKey 密钥
     * @throws JWTVerificationException 验证失败异常
     */
    public static void verifyToken(String token, String secretKey) throws JWTVerificationException {
        verifyToken(token, secretKey, 60);
    }

    /**
     * 验证JWT Token（自定义时间偏差）
     *
     * @param token        Token
     * @param secretKey    密钥
     * @param jwtTimeSeconds 时间偏差（秒）
     * @throws JWTVerificationException 验证失败异常
     */
    public static void verifyToken(String token, String secretKey, int jwtTimeSeconds) throws JWTVerificationException {
        // 移除 "Bearer " 前缀
        String actualToken = removeBearerPrefix(token);

        // 解码JWT
        DecodedJWT decodedJWT = JWT.decode(actualToken);

        // 创建验证器
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                .acceptIssuedAt((long) jwtTimeSeconds)
                .build();

        // 验证Token
        verifier.verify(actualToken);
    }

    /**
     * 验证JWT Token并返回Issuer
     *
     * @param token     Token
     * @param secretKey 密钥
     * @return Issuer（应用ID）
     * @throws JWTVerificationException 验证失败异常
     */
    public static String verifyAndGetIssuer(String token, String secretKey) throws JWTVerificationException {
        String actualToken = removeBearerPrefix(token);

        DecodedJWT decodedJWT = JWT.decode(actualToken);

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                .acceptIssuedAt(60)
                .build();

        verifier.verify(actualToken);

        return decodedJWT.getIssuer();
    }

    /**
     * 移除Token的 "Bearer " 前缀
     *
     * @param token Token
     * @return 处理后的Token
     */
    public static String removeBearerPrefix(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        return token.replaceFirst("^Bearer\\s+", "").trim();
    }

    /**
     * 解码JWT Token（不验证）
     *
     * @param token Token
     * @return DecodedJWT
     */
    public static DecodedJWT decodeToken(String token) {
        String actualToken = removeBearerPrefix(token);
        return JWT.decode(actualToken);
    }

    /**
     * 从请求头中提取Token
     *
     * @param authorizationHeader Authorization请求头
     * @return Token
     */
    public static String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }

    /**
     * 检查Token是否即将过期
     *
     * @param token Token
     * @param secretKey 密钥
     * @param seconds 秒数
     * @return true-即将过期，false-未过期
     */
    public static boolean isTokenExpiringSoon(String token, String secretKey, int seconds) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            if (expiresAt == null) {
                return false;
            }
            long now = System.currentTimeMillis();
            long expiresIn = expiresAt.getTime() - now;
            return expiresIn <= seconds * 1000L;
        } catch (Exception e) {
            logger.error("Check token expire time error", e);
            return true;
        }
    }
}
