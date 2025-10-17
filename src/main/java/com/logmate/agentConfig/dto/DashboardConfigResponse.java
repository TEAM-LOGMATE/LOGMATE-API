package com.logmate.agentConfig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardConfigResponse {
    private Long dashboardId;
    private PullerConfig pullerConfig;
    private List<WatcherConfig> logPipelineConfigs;
    private String dashboardStatus;
}