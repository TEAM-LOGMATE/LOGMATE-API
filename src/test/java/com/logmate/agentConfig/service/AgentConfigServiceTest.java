package com.logmate.agentConfig.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.ConfigDTO;
import com.logmate.agentConfig.dto.SaveDashboardConfigRequest;
import com.logmate.agentConfig.model.AgentConfiguration;
import com.logmate.agentConfig.repository.AgentConfigurationRepository;
import com.logmate.agentConfig.repository.LogPipelineConfigRepository;
import com.logmate.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentConfigServiceTest {

    @Mock
    AgentConfigurationRepository configRepo;

    @Mock
    LogPipelineConfigRepository pipelineRepo;

    @Mock
    DashboardService dashboardService;

    AgentConfigService service;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AgentConfigService(configRepo, pipelineRepo, objectMapper, dashboardService);
    }

    @Test
    @DisplayName("saveConfig - 새로운 AgentConfig 생성")
    void saveConfig_newAgent() {
        SaveDashboardConfigRequest req = new SaveDashboardConfigRequest();
        req.setAgentId("");
        req.setLogPipelineConfigs(new ArrayList<>());

        String result = service.saveConfig(req, 1L);

        assertThat(result).isNotBlank();
        verify(configRepo, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("getConfig - etag 동일하면 null 반환")
    void getConfig_etagSame() {
        AgentConfiguration config = new AgentConfiguration("agent1", "etag-123", "{}");

        when(configRepo.findByAgentId("agent1"))
                .thenReturn(Optional.of(config));

        ConfigDTO result = service.getConfig("agent1", "etag-123");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getConfig - etag 다르면 ConfigDTO 반환")
    void getConfig_etagDifferent() {
        AgentConfiguration config = new AgentConfiguration("agent1", "etag-old", "{}");

        when(configRepo.findByAgentId("agent1"))
                .thenReturn(Optional.of(config));
        when(pipelineRepo.findByAgentConfiguration(config))
                .thenReturn(List.of()); //파이프라인 없다 가정함

        ConfigDTO dto = service.getConfig("agent1", "different");

        assertThat(dto).isNotNull();
    }
}
