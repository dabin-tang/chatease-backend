package com.dbt.chatease.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "app_version")
public class AppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version_number", nullable = false)
    private String versionNumber; 
    
    @Column(name = "update_content", columnDefinition = "TEXT")
    private String updateContent; 
    
    @Column(name = "download_url", nullable = false)
    private String downloadUrl;   
    
    @Column(name = "file_size")
    private Long fileSize;        
    
    @Column(name = "status")
    private Integer status;       // 1: Published
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
}