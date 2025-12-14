package com.dbt.chatease.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_setting")
public class SysSetting {
    @Id
    @Column(name = "setting_code", length = 50, nullable = false)
    private String settingCode;

    @Column(name = "setting_value", length = 1000)
    private String settingValue;

    @Column(name = "description", length = 200)
    private String description;
}