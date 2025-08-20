package com.logmate.agentConfig.dto;

import lombok.Data;
import java.util.List;

@Data
public class ConfigDTO {
    private String etag;
    private AgentConfig agentConfig;
    private PullerConfig pullerConfig;
    private List<WatcherConfig> watcherConfigs;
}