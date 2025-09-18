package com.logmate.agentConfig.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveDashboardConfigRequest {

    @Data
    public static class TailerRequest {
        private int readIntervalMs;
        private String metaDataFilePathPrefix;
    }

    @Data
    public static class MultilineRequest {
        private boolean enabled;
        private int maxLines;
    }

    @Data
    public static class ExporterRequest {
        private Boolean compressEnabled;
        private int retryIntervalSec;
        private int maxRetryCount;
    }

    @Data
    public static class FilterRequest {
        private List<String> allowedLevels;
        private List<String> requiredKeywords;
        private LocalDateTime after;
    }

    @Data
    public static class PullerRequest {
        private int intervalSec;
    }

    private TailerRequest tailer;
    private MultilineRequest multiline;
    private ExporterRequest exporter;
    private FilterRequest filter;
    private PullerRequest puller;
}