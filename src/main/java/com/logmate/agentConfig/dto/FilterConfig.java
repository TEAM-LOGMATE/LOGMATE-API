package com.logmate.agentConfig.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Predicate;


@Data
public class FilterConfig {
    private Set<String> allowedLevels;
    private Set<String> allowedLoggers;
    private Set<String> requiredKeywords;
    private LocalDateTime after;


    @JsonIgnore
    private Predicate<Object> customPredicate;
}