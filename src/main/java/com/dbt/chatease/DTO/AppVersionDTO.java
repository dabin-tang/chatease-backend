package com.dbt.chatease.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AppVersionDTO {
    /**
     * Version number (e.g., 1.0.0)
     */
    private String versionNumber; 
    
    /**
     * Update description text
     */
    private String updateContent; 
    
    /**
     * The installer file (.exe, .dmg, .zip)
     */
    private MultipartFile file;   
}