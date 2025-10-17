package com.logmate.agentConfig.repository;

import com.logmate.agentConfig.model.AgentConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AgentConfigurationRepository extends JpaRepository<AgentConfiguration, Long> {
    Optional<AgentConfiguration> findByAgentId(String agentId);
    @Query("SELECT DISTINCT a FROM AgentConfiguration a JOIN LogPipelineConfig l ON l.agentConfiguration = a WHERE l.dashboardId = :dashboardId")
    Optional<AgentConfiguration> findByDashboardId(@Param("dashboardId") Long dashboardId);
}