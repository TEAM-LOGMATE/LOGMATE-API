package com.logmate.agentConfig.dto;

import lombok.Data;


@Data
public class PullerConfig {
    private String pullURL;
    private int intervalSec;
    private String etag;
}