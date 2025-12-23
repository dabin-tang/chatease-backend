package com.dbt.chatease.Config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    //Ensure this matches UploadController's directory!!!
    @Value("${file.upload-dir:C:\\imgStore}")
    private String baseUploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //Regular User Interceptor (Intercepts all paths /**)
        //Path exclusions are handled by EXCLUDE_PATHS in JwtInterceptor
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**");

        //Admin interceptor (intercepts /admin/**)
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + baseUploadDir;
        if (!location.endsWith("\\") && !location.endsWith("/")) {
            location += File.separator;
        }

        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }

}