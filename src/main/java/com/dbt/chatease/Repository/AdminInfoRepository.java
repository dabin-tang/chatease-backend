package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.AdminInfo;
import com.dbt.chatease.Entity.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminInfoRepository extends JpaRepository<AdminInfo, Integer> {
    AdminInfo findByUsername(String username);


}