package com.logmate.agentConfig.dto;

import lombok.Data;


@Data
public class WatcherConfig {
    private String etag;
    private Integer thNum;
    private TailerConfig tailer;
    private MultilineConfig multiline;
    private ExporterConfig exporter;
    private ParserConfig parser;
    private FilterConfig filter;
}