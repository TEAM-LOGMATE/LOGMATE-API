package com.logmate.agentConfig.dto;

import lombok.Data;


@Data
public class ExporterConfig {
    private String pushURL;
    private Boolean compressEnabled;
    private int retryIntervalSec;
    private int maxRetryCount;
}