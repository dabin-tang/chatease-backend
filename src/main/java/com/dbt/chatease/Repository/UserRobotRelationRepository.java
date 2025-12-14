package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.UserRobotRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRobotRelationRepository extends JpaRepository<UserRobotRelation, Long> {

    /**
     * Find robot relations by user ID and status.
     * Used when fetching the session list (to show the robot).
     *
     * @param userId The user ID
     * @param status Status (e.g., 1 for Normal)
     * @return List of relations
     */
    List<UserRobotRelation> findByUserIdAndStatus(String userId, Integer status);
    
    /**
     * Check if a relationship already exists (to prevent duplicates).
     */
    boolean existsByUserIdAndRobotId(String userId, String robotId);
}