package com.logmate.agentConfig.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;


@Data
public class FilterConfig {
    //Tomcat
    private Set<String> allowedMethods;

    //SpringBoot
    private Set<String> allowedLevels;
    private Set<String> requiredKeywords;
}