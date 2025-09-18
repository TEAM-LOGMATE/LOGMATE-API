package com.logmate.agentConfig.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;


@Data
public class FilterConfig {
    private Set<String> allowedLevels;
    private Set<String> allowedLoggers;
    private Set<String> requiredKeywords;
    private LocalDateTime after;
}