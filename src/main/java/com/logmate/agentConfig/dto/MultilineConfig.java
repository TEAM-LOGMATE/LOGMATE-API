package com.logmate.agentConfig.dto;

import lombok.Data;

@Data
public class MultilineConfig {
    private boolean enabled;
    private int maxLines;
}