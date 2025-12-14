package com.dbt.chatease.Utils;

import com.dbt.chatease.DTO.UserInfoDTO;
import com.dbt.chatease.Entity.AdminInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    
    private final SecretKey SECRET_KEY;
    
    @Value("${jwt.expiration-time:86400000}")
    private long expirationTime;
    
    //Constructor with configurable secret key
    public JwtUtil(@Value("${jwt.secret:default-secret-key-change-in-production}") String secret) {
        //Ensure key length is sufficient for HS256
        String key = secret.length() < 32 ? 
            String.format("%-32s", secret).replace(' ', '0') : 
            secret.substring(0, 32);
        this.SECRET_KEY = Keys.hmacShaKeyFor(key.getBytes());
    }
    
    /**
     * Generate JWT token containing basic user information
     */
    public String generateToken(UserInfoDTO userInfoDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userInfoDTO.getUserId());
        claims.put("email", userInfoDTO.getEmail());
        claims.put("nickName", userInfoDTO.getNickName());
        claims.put("avatar", userInfoDTO.getAvatar());
        
        return Jwts.builder()
                .claims(claims)
                .subject(userInfoDTO.getUserId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY, Jwts.SIG.HS256)
                .compact();
    }
    
    /**
     * Validate token validity
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Extract user information from token
     */
    public UserInfoDTO getUserInfoFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserId(claims.get("userId", String.class));
        userInfoDTO.setEmail(claims.get("email", String.class));
        userInfoDTO.setNickName(claims.get("nickName", String.class));
        userInfoDTO.setAvatar(claims.get("avatar", String.class));
        
        return userInfoDTO;
    }
    
    /**
     * Extract user ID from token
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
    /**
     * Get token expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
    
    /**
     * Refresh token with new expiration
     */
    public String refreshToken(String oldToken) {
        if (!validateToken(oldToken)) {
            throw new JwtException("Invalid token");
        }
        
        UserInfoDTO userInfo = getUserInfoFromToken(oldToken);
        return generateToken(userInfo);
    }

    public String generateAdminToken(AdminInfo adminInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminInfo.getAdminId());
        claims.put("username", adminInfo.getUsername());
        claims.put("role", adminInfo.getRole());
        claims.put("type", "ADMIN");

        return Jwts.builder()
                .claims(claims)
                .subject(adminInfo.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY, Jwts.SIG.HS256)
                .compact();
    }


    public String getRoleFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}