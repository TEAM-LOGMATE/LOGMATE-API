package com.logmate.agentConfig.dto;

import lombok.Data;


@Data
public class TailerConfig {
    private String filePath;
    private int readIntervalMs;
    private String metaDataFilePathPrefix;
}
