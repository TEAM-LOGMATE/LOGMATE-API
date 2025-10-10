package com.logmate.agentConfig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardConfigResponse {
    private Long dashboardId;
    private List<WatcherConfig> logPipelineConfigs;
}