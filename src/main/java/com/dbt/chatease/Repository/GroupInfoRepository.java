package com.dbt.chatease.Repository;

import com.dbt.chatease.DTO.GroupInfoDTO;
import com.dbt.chatease.Entity.GroupInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupInfoRepository extends JpaRepository<GroupInfo, String> {
    @Modifying
    @Query("UPDATE GroupInfo g SET " +
            "g.groupName = COALESCE(:#{#dto.groupName}, g.groupName), " +
            "g.groupAvatar = COALESCE(:#{#dto.groupAvatar}, g.groupAvatar), " +
            "g.groupNotice = COALESCE(:#{#dto.groupNotice}, g.groupNotice), " +
            "g.joinType = COALESCE(:#{#dto.joinType}, g.joinType) " +
            "WHERE g.groupId = :#{#dto.groupId}")
    int updateGroupInfo(@Param("dto") GroupInfoDTO dto);


    @Query("SELECT gi FROM UserContact uc JOIN GroupInfo gi ON uc.contactId = gi.groupId WHERE uc.userId = :userId AND uc.contactType = 1 ORDER BY uc.createTime DESC")
    List<GroupInfo> findGroupsByUserId(@Param("userId") String userId);

    @Query("SELECT g FROM GroupInfo g WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(g.groupName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "g.groupId LIKE CONCAT('%', :keyword, '%'))")
    Page<GroupInfo> searchGroups(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE GroupInfo g SET g.status = :status " +
            "WHERE g.groupId = :groupId AND g.groupOwnerId = :groupOwnerId")
    int updateGroupStatus(@Param("groupId") String groupId,
                          @Param("groupOwnerId") String groupOwnerId,
                          @Param("status") Integer status);
}