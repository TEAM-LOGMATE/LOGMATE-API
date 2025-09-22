package com.logmate.agentConfig.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_pipeline_configs")
@Getter
@NoArgsConstructor
public class LogPipelineConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String etag;

    private Integer thNum;

    private String filePath;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String configJson; // watcherConfig 전체 JSON (tailer, multiline, exporter, parser, filter 포함)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_config_id", nullable = false)
    private AgentConfiguration agentConfiguration;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public LogPipelineConfig(String etag, Integer thNum, String configJson, AgentConfiguration agentConfiguration) {
        this.etag = etag;
        this.thNum = thNum;
        this.configJson = configJson;
        this.agentConfiguration = agentConfiguration;
        this.createdAt = LocalDateTime.now();
    }
    public void update(String newEtag, String newFilePath, String newConfigJson) {
        this.etag = newEtag;
        this.filePath = newFilePath;
        this.configJson = newConfigJson;
    }
}