package com.logmate.agentConfig;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_configurations")
@Getter
@NoArgsConstructor
public class AgentConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String agentId;

    @Column(nullable = false)
    private String etag;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String configJson; //ConfigDTO 전체를 JSON으로 저장

    private LocalDateTime createdAt;

    public AgentConfiguration(String agentId, String etag, String configJson) {
        this.agentId = agentId;
        this.etag = etag;
        this.configJson = configJson;
        this.createdAt = LocalDateTime.now();
    }
}
