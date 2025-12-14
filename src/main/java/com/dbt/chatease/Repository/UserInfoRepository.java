package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    UserInfo findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE UserInfo u SET " +
            "u.nickName = COALESCE(:nickName, u.nickName), " +
            "u.avatar = COALESCE(:avatar, u.avatar), " +
            "u.joinType = COALESCE(:joinType, u.joinType), " +
            "u.sex = COALESCE(:sex, u.sex), " +
            "u.personalSignature = COALESCE(:personalSignature, u.personalSignature), " +
            "u.areaName = COALESCE(:areaName, u.areaName), " +
            "u.areaCode = COALESCE(:areaCode, u.areaCode) " +
            "WHERE u.userId = :userId")
    int updateUserInfo(@Param("userId") String userId,
                       @Param("nickName") String nickName,
                       @Param("avatar") String avatar,
                       @Param("joinType") Integer joinType,
                       @Param("sex") Integer sex,
                       @Param("personalSignature") String personalSignature,
                       @Param("areaName") String areaName,
                       @Param("areaCode") String areaCode);

    @Modifying
    @Query("UPDATE UserInfo u SET u.password = :password WHERE u.userId = :userId")
    int updatePassword(@Param("userId") String userId, @Param("password") String password);

    @Modifying
    @Query("UPDATE UserInfo u SET u.lastOffTime = :logoutTime WHERE u.userId = :userId")
    int updateLogoutTime(@Param("userId") String userId, @Param("logoutTime") Long logoutTime);

    @Query("SELECT u FROM UserInfo u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.nickName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.userId LIKE CONCAT('%', :keyword, '%'))")
    Page<UserInfo> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE UserInfo u SET u.status = :status WHERE u.userId = :userId")
    int updateUserStatus(@Param("userId") String userId, @Param("status") Integer status);
}