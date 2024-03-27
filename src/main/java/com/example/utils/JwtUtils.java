package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    Integer expire;

    @Resource
    StringRedisTemplate template;

    /**
     * 用于创建一个Jwt令牌
     * @param details UserDetails
     * @param id ID
     * @param username username
     * @return JWT
     */
    public String createJwt(UserDetails details, Integer id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(this.expireTime())
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    /**
     * 验证Jwt令牌是否有效
     * @param headerToken token
     * @return DecodedJWT
     */
    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            if (this.isInvalidToken(verify.getId())) return null;
            return new Date().after(verify.getExpiresAt()) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 获取Jwt令牌的失效时间
     * @return Date
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    /**
     * 验证Jwt令牌的合法性
     * @param headerToken headerToken
     * @return token
     */
    private String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer ")) {
            return null;
        }
        return headerToken.substring(7);
    }

    /**
     * 解析Jwt，获取用户信息
     * @param jwt jwt
     * @return UserDetails
     */
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * 解析Jwt，获取用户ID
     * @param jwt jwt
     * @return 用户ID
     */
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }


    /**
     * 将Jwt令牌拉入黑名单
     * @param headerToken headerToken
     * @return 是否拉黑成功
     */
    public boolean invalidataJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) return false;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = jwtVerifier.verify(token);
            return doInvalidataJwt(jwt.getId(), jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 将Jwt令牌拉入黑名单
     * @param uuid jwtId
     * @param expireDate jwt失效时间
     * @return 是否添加
     */
    private boolean doInvalidataJwt(String uuid, Date expireDate) {
        if (this.isInvalidToken(uuid)) return false;
        Date now = new Date();
        long expire = Math.max(expireDate.getTime() - now.getTime(), 0);
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 判断Jwt是否存在于黑名单中
     * @param uuid jwtId
     * @return 是否存在
     */
    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
}
