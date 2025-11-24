package com.logmate.agentConfig.repository;

import com.logmate.agentConfig.model.AgentConfiguration;
import com.logmate.agentConfig.model.LogPipelineConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LogPipelineConfigRepositoryTest {

    @Autowired
    LogPipelineConfigRepository pipelineRepo;

    @Autowired
    AgentConfigurationRepository configRepo;

    @Test
    @DisplayName("findByAgentConfigurationAndFilePathAndDashboardId -파이프라인 조회")
    void findByAgentConfigurationAndFilePathAndDashboardId_success() {
        //given
        AgentConfiguration agent = configRepo.save(new AgentConfiguration("agent-3", "etag-3", "{}"));

        LogPipelineConfig p1 = new LogPipelineConfig(
                "etag-p1",
                1,
                "/var/log/app.log",
                "{}",
                agent,
                10L
        );
        pipelineRepo.save(p1);

        //when
        LogPipelineConfig found =
                pipelineRepo.findByAgentConfigurationAndFilePathAndDashboardId(agent, "/var/log/app.log", 10L);

        //then
        assertThat(found).isNotNull();
        assertThat(found.getDashboardId()).isEqualTo(10L);
        assertThat(found.getFilePath()).isEqualTo("/var/log/app.log");
    }

    @Test
    @DisplayName("findByDashboardIdIn - 여러 dashboardId로 파이프라인 목록 조회")
    void findByDashboardIdIn_success() {
        //given
        AgentConfiguration agent = configRepo.save(new AgentConfiguration("agent-4", "etag-4", "{}"));

        pipelineRepo.save(new LogPipelineConfig("e1", 1, "/var/log/a.log", "{}", agent, 1L));
        pipelineRepo.save(new LogPipelineConfig("e2", 2, "/var/log/b.log", "{}", agent, 2L));
        pipelineRepo.save(new LogPipelineConfig("e3", 3, "/var/log/c.log", "{}", agent, 3L));

        //when
        List<LogPipelineConfig> result =
                pipelineRepo.findByDashboardIdIn(List.of(1L, 3L));

        //then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(LogPipelineConfig::getDashboardId)
                .containsExactlyInAnyOrder(1L, 3L);
    }
}