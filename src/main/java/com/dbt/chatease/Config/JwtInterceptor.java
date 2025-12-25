package com.dbt.chatease.Config;

import com.dbt.chatease.DTO.UserInfoDTO;
import com.dbt.chatease.Utils.JwtUtil;
import com.dbt.chatease.Utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    //Paths that don't require JWT authentication
    private static final String[] EXCLUDE_PATHS = {
//            "/api/auth/login",
//            "/api/auth/register",
//            "/api/auth/refresh-token",
//            "/swagger-ui/**",
//            "/v3/api-docs/**",
//            "/error",
//            "/api/app-version/latest"
            "/user-info/login",
            "/user-info/register",
            "/user-info/sendCode",
            "/user-info/reset-password",

            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error",
            "/files/**",

            "/api/app-version/latest",
            "/ws/**"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/files/")) {
            return true;
        }
        if (requestURI.startsWith("/ws/")) {
            return true;
        }
        //Skip JWT check for excluded paths
        for (String path : EXCLUDE_PATHS) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        //Extract Token from header
        String token = extractTokenFromRequest(request);
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Missing token\"}");
            return false;
        }
        //Validate Token signature and expiration
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Invalid or expired token\"}");
            return false;
        }
        //Check Redis Blacklist (For Banned Users or Forced Logout)
        //If the key "BANNED:userId" exists, deny access immediately.
        String userId = jwtUtil.getUserIdFromToken(token);
        String banKey = "BANNED:" + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(banKey))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Your account has been banned or forced logout.\"}");
            return false;
        }
        //Store user info in ThreadLocal for current request
        UserInfoDTO userInfo = jwtUtil.getUserInfoFromToken(token);
        UserContext.setUser(userInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Clear ThreadLocal after request completion to prevent memory leaks
        UserContext.clear();
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}