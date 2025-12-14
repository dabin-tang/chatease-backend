package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.UserApply;
import com.dbt.chatease.VO.FriendRequestVO;
import com.dbt.chatease.VO.GroupRequestVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserApplyRepository extends JpaRepository<UserApply, Integer> {
    UserApply findByApplyUserIdAndReceiveUserIdAndContactIdAndContactType(
            String applyUserId, String receiveUserId, String contactId, Integer contactType);

    @Query("SELECT " +
            "ua as userApply, " +
            "ui.nickName as applyUserNickName, " +
            "ui.avatar as applyUserAvatar " +
            "FROM UserApply ua " +
            "JOIN UserInfo ui ON ua.applyUserId = ui.userId " +
            "WHERE ua.receiveUserId = :receiveUserId AND ua.contactType = 0 " +
            "ORDER BY ua.lastApplyTime DESC")
    Page<FriendRequestVO> findFriendRequestsWithUserInfo(@Param("receiveUserId") String receiveUserId, Pageable pageable);

    @Query("SELECT " +
            "ua as userApply, " +
            "ui.nickName as applyUserNickName, " +
            "ui.avatar as applyUserAvatar, " +
            "gi.groupName as groupName, " +
            "gi.groupAvatar as groupAvatar " +
            "FROM UserApply ua " +
            "JOIN UserInfo ui ON ua.applyUserId = ui.userId " +
            "JOIN GroupInfo gi ON ua.contactId = gi.groupId " +
            "WHERE ua.receiveUserId = :receiveUserId AND ua.contactType = 1 " +
            "ORDER BY ua.lastApplyTime DESC")
    Page<GroupRequestVO> findGroupRequestsWithInfo(@Param("receiveUserId") String receiveUserId, Pageable pageable);


}