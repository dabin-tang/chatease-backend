package com.dbt.chatease.Config;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public EmailValidator emailValidator() {
        return EmailValidator.getInstance();
    }

}