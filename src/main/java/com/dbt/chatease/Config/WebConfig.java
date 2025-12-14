package com.dbt.chatease.Config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtInterceptor jwtInterceptor;
    @Autowired
    private AdminInterceptor adminInterceptor;


    //    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(jwtInterceptor)
//                .addPathPatterns("/api/**")          // Protect all API endpoints
//                .excludePathPatterns("/api/auth/**"); // Exclude authentication endpoints
//
//        registry.addInterceptor(adminInterceptor)
//                .addPathPatterns("/admin/**")
//                .excludePathPatterns("/admin/login");
//    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 普通用户拦截器 (拦截所有路径 /**)
        // 具体的排除逻辑交给 JwtInterceptor 内部的 EXCLUDE_PATHS 处理
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**");

        // 2. 管理员拦截器 (拦截 /admin/**)
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL /files/** to local disk directory
        // This covers both images (/files/chateaseimg/...) and apps (/files/app_packages/...)
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:C:\\imgStore\\");
    }
}