package com.logmate.agentConfig.repository;

import com.logmate.agentConfig.model.AgentConfiguration;
import com.logmate.agentConfig.model.LogPipelineConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogPipelineConfigRepository extends JpaRepository<LogPipelineConfig, Long> {
    List<LogPipelineConfig> findByAgentConfiguration(AgentConfiguration agentConfig);
    LogPipelineConfig findByAgentConfigurationAndFilePath(AgentConfiguration agentConfig, String filePath);
    List<LogPipelineConfig> findByDashboardId(Long dashboardId);
    LogPipelineConfig findByAgentConfigurationAndFilePathAndDashboardId(
            AgentConfiguration agentConfig, String filePath, Long dashboardId);
    List<LogPipelineConfig> findByDashboardIdIn(List<Long> dashboardIds);

}
