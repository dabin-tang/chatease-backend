package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.SysSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysSettingRepository extends JpaRepository<SysSetting, String> {
    // No extra methods needed. 
    // JpaRepository provides findById(String settingCode) out of the box.
}