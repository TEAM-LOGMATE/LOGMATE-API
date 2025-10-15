package com.logmate.agentConfig.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveDashboardConfigRequest {
    private String agentId; // NULL 이면 신규

    @Data
    public static class TailerRequest {
        private String filePath; // 요청시에 한번 더 받아야 할듯
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
        private String pushURL; //streaming server 경로로 서버에서 채워주기
        private Boolean compressEnabled;
        private int retryIntervalSec;
        private int maxRetryCount;
    }

    @Data
    public static class FilterRequest {
        // SpringBoot
        private List<String> allowedLevels;
        private List<String> requiredKeywords;

        // Tomcat
        private List<String> allowedMethods;
    }

    @Data
    public static class PullerRequest {
        private int intervalSec;
    }

    @Data
    public static class ParserConfigRequest {
        private String timezone; //e.g. Asia/Seoul
    }

    @Data
    public static class WatcherRequest {
        private TailerRequest tailer;
        private MultilineRequest multiline;
        private ExporterRequest exporter;
        private FilterRequest filter;
        private String parserType; // "springboot" or "tomcat"
        private ParserConfig.ParserDetailConfig parser;
    }

    private PullerRequest puller;
    private List<WatcherRequest> logPipelineConfigs;
}