package com.logmate.agentConfig.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agent_configurations")
@Getter
@NoArgsConstructor
public class AgentConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime lastUpdatedAt;

    @Column(nullable = false, unique = true, updatable = false)
    private String agentId;

    @Column(nullable = false)
    private String etag;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String configJson; //pullerConfig, agentConfig 객체용 JSON

    private LocalDateTime createdAt;

    //Agent가 마지막으로 pulling한 시각
    private LocalDateTime lastPulledAt;

    //Agent가 한 번이라도 pulling 요청을 보냈는지 여부
    @Column(nullable = false)
    private boolean pulledOnce = false;

    // logPipelineConfigs 관계 설정
    @OneToMany(mappedBy = "agentConfiguration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LogPipelineConfig> logPipelineConfigs = new ArrayList<>();

    @PrePersist //생성 시 자동으로 시간 기록
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = this.createdAt;


        if (this.agentId == null) {
            this.agentId = UUID.randomUUID().toString(); //UUID 자동 생성
        }
    }

    public void update(String newEtag, String newConfigJson) {
        this.etag = newEtag;
        this.configJson = newConfigJson;
        this.lastUpdatedAt = LocalDateTime.now();
    }


    public AgentConfiguration(String agentId, String etag, String configJson) {
        this.agentId = agentId;
        this.etag = etag;
        this.configJson = configJson;
        this.createdAt = LocalDateTime.now();
    }

    // 동시에 갱신하는 메서드
    public void markPulled() {
        this.lastPulledAt = LocalDateTime.now(); //agent get 요청시각
        this.pulledOnce = true;  //한번이라도 pulling 요청 있었는지
    }
}
