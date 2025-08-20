package com.logmate.agentConfig;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentConfigurationRepository extends JpaRepository<AgentConfiguration, Long> {
    Optional<AgentConfiguration> findTopByAgentIdOrderByCreatedAtDesc(String agentId);
}