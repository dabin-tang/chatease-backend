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

    //Get requests by receiver ID and type (0: Friend, 1: Group)
    Page<UserApply> findByReceiveUserIdAndContactTypeOrderByLastApplyTimeDesc(
            String receiveUserId, Integer contactType, Pageable pageable);

}