package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.SysBroadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SysBroadcastRepository extends JpaRepository<SysBroadcast, Long> {

    /**
     * Find broadcasts created after a specific time.
     * Used to pull system messages sent AFTER the user registered.
     *
     * @return List of broadcast messages
     */
    List<SysBroadcast> findByCreateTimeAfter(LocalDateTime time);

    SysBroadcast findTopByOrderByCreateTimeDesc();
}