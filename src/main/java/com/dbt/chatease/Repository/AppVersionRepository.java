package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
    /**
     * Get the latest published version
     * Status = 1 means published
     */
    AppVersion findTopByStatusOrderByCreateTimeDesc(Integer status);
}