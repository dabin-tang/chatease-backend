package com.dbt.chatease.Repository;

import com.dbt.chatease.DTO.GroupMemberDTO;
import com.dbt.chatease.Entity.UserContact;
import com.dbt.chatease.VO.ContactVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, UserContact.UserContactId> {

    Optional<UserContact> findByUserIdAndContactId(String userId, String contactId);

    Long countByContactIdAndContactType(String contactId, Integer contactType);

    /**
     * Get group members with their user info.
     */
    @Query("SELECT new com.dbt.chatease.DTO.GroupMemberDTO(uc, ui.nickName, ui.avatar, ui.sex) " +
            "FROM UserContact uc JOIN UserInfo ui ON uc.userId = ui.userId " +
            "WHERE uc.contactId = :groupId AND uc.contactType = 1 AND uc.status = 1 " +
            "ORDER BY uc.createTime DESC")
    List<GroupMemberDTO> findGroupMembersWithUserInfo(@Param("groupId") String groupId);

    boolean existsByUserIdAndContactIdAndContactType(String userId, String contactId, Integer contactType);

    UserContact findByUserIdAndContactIdAndContactType(String userId, String contactId, Integer contactType);

    /**
     * Find friend contacts with UserInfo joined.
     */
    @Query("SELECT new com.dbt.chatease.VO.ContactVO(uc, ui.nickName, ui.avatar, null, null) " +
            "FROM UserContact uc " +
            "JOIN UserInfo ui ON uc.contactId = ui.userId " +
            "WHERE uc.userId = :userId AND uc.contactType = 0 AND uc.status = 1 " +
            "ORDER BY uc.createTime DESC")
    List<ContactVO> findFriendContacts(@Param("userId") String userId);

    /**
     * Find group contacts with GroupInfo joined.
     */
    @Query("SELECT new com.dbt.chatease.VO.ContactVO(uc, null, null, gi.groupName, gi.groupAvatar) " +
            "FROM UserContact uc " +
            "JOIN GroupInfo gi ON uc.contactId = gi.groupId " +
            "WHERE uc.userId = :userId AND uc.contactType = 1 AND uc.status = 1 " +
            "ORDER BY uc.createTime DESC")
    List<ContactVO> findGroupContacts(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserContact uc SET uc.status = :status, uc.lastUpdateTime = :updateTime " +
            "WHERE uc.userId = :userId AND uc.contactId = :contactId AND uc.contactType = :contactType")
    int updateByUserIdAndContactIdAndContactType(@Param("userId") String userId,
                                                 @Param("contactId") String contactId,
                                                 @Param("contactType") Integer contactType,
                                                 @Param("status") Integer status,
                                                 @Param("updateTime") LocalDateTime updateTime);

    @Modifying
    @Query("UPDATE UserContact uc SET uc.status = :status, uc.lastUpdateTime = :updateTime " +
            "WHERE uc.contactId = :contactId AND uc.contactType = :contactType")
    int updateStatusByContactId(@Param("contactId") String contactId,
                                @Param("contactType") Integer contactType,
                                @Param("status") Integer status,
                                @Param("updateTime") LocalDateTime updateTime);

    List<UserContact> findByContactIdAndContactType(String contactId, Integer contactType);

    /**
     * Find the earliest member in a group to transfer ownership.
     */
    @Query("SELECT uc FROM UserContact uc " +
            "WHERE uc.contactId = :groupId AND uc.contactType = 1 AND uc.status = 1 " +
            "AND uc.userId <> :excludeUserId " +
            "ORDER BY uc.createTime ASC")
    List<UserContact> findEarliestMembers(@Param("groupId") String groupId, @Param("excludeUserId") String excludeUserId, Pageable pageable);

    List<UserContact> findByUserIdAndContactType(String userId, Integer contactType);

}