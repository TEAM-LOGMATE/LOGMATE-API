package com.logmate.agentConfig.repository;

import com.logmate.agentConfig.model.AgentConfiguration;
import com.logmate.agentConfig.model.LogPipelineConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AgentConfigurationRepositoryTest {

    @Autowired
    AgentConfigurationRepository configRepo;

    @Autowired
    LogPipelineConfigRepository pipelineRepo;

    @Test
    @DisplayName("findByAgentId - agentId로 AgentConfiguration 조회")
    void findByAgentId_success() {
        //given
        AgentConfiguration config = new AgentConfiguration("agent-1", "etag-1", "{}");
        configRepo.save(config);

        //when
        Optional<AgentConfiguration> result = configRepo.findByAgentId("agent-1");

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getAgentId()).isEqualTo("agent-1");
        assertThat(result.get().getEtag()).isEqualTo("etag-1");
    }

    @Test
    @DisplayName("findByDashboardId - dashboardId에 연결된 AgentConfiguration 조회")
    void findByDashboardId_success() {
        //given
        AgentConfiguration agent =
                configRepo.save(new AgentConfiguration("agent-2", "etag-2", "{}"));

        LogPipelineConfig pipeline = new LogPipelineConfig(
                "p-etag",
                1,
                "/var/log/app.log",
                "{}",
                agent,
                99L
        );
        pipelineRepo.save(pipeline);

        //when
        Optional<AgentConfiguration> result = configRepo.findByDashboardId(99L);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getAgentId()).isEqualTo("agent-2");
    }
}