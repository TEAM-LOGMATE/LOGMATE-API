package com.logmate.agentConfig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.*;
import com.logmate.agentConfig.service.AgentConfigService;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AgentConfigControllerTest {
    @InjectMocks
    AgentConfigController controller;

    @Mock
    AgentConfigService agentConfigService;

    @Mock
    UserRepository userRepository;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("saveConfig - 대시보드 고급설정 저장 성공")
    void saveConfig_success() throws Exception {
        SaveDashboardConfigRequest req = new SaveDashboardConfigRequest();
        req.setAgentId("");
        req.setLogPipelineConfigs(new ArrayList<>());

        when(agentConfigService.saveConfig(any(SaveDashboardConfigRequest.class), eq(2L)))
                .thenReturn("agent-123");

        mockMvc.perform(post("/api/folders/{folderId}/dashboards/{dashboardId}/config", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("대시보드 고급설정 저장 성공"))
                .andExpect(jsonPath("$.data.agentId").value("agent-123"));
    }

    @Test
    @DisplayName("getConfig - etag 동일하면 304 NOT_MODIFIED + BaseResponse")
    void getConfig_notModified() throws Exception {
        String email = "test@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(new User()));

        //서비스에서 null 반환하면 ->컨트롤러에서 304
        when(agentConfigService.getConfig("agent-1", "etag-1"))
                .thenReturn(null);

        mockMvc.perform(get("/api/config")
                        .param("agentId", "agent-1")
                        .param("etag", "etag-1")
                        .requestAttr("email", email))
                .andExpect(status().isNotModified())
                .andExpect(jsonPath("$.status").value(304))
                .andExpect(jsonPath("$.message").value("변경된 설정 없음"));
    }

    @Test
    @DisplayName("getConfig - 변경된 설정 있으면 ConfigDTO 200으로 반환")
    void getConfig_success() throws Exception {
        String email = "test@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(new User()));

        ConfigDTO dto = new ConfigDTO();
        dto.setEtag("new-etag");
        dto.setAgentConfig(null);
        dto.setPullerConfig(null);
        dto.setLogPipelineConfigs(new ArrayList<>());

        when(agentConfigService.getConfig("agent-1", "old-etag"))
                .thenReturn(dto);

        mockMvc.perform(get("/api/config")
                        .param("agentId", "agent-1")
                        .param("etag", "old-etag")
                        .requestAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etag").value("new-etag"));
    }

    @Test
    @DisplayName("updatePipeline - LogPipeline 업데이트 성공")
    void updatePipeline_success() throws Exception {
        String email = "test@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(new User()));

        Map<String, Object> body = Map.of(
                "agentId", "agent-1",
                "targetFilePath", "/var/log/app.log",
                "logPipelineConfig", Map.of(), // 내용 단순화
                "puller", Map.of("intervalSec", 20)
        );

        mockMvc.perform(put("/api/folders/{folderId}/dashboards/{dashboardId}/config", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .requestAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("LogPipeline 업데이트 성공"))
                .andExpect(jsonPath("$.data.agentId").value("agent-1"))
                .andExpect(jsonPath("$.data.updatedFilePath").value("/var/log/app.log"));
    }

    @Test
    @DisplayName("getConfigsByFolder - 폴더 내 대시보드 고급설정 목록 조회 성공")
    void getConfigsByFolder_success() throws Exception {
        String email = "test@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(new User()));

        PullerConfig puller = new PullerConfig();
        puller.setPullURL("https://www.logmate.shop");
        puller.setIntervalSec(10);

        List<WatcherConfig> watchers = new ArrayList<>();
        DashboardConfigResponse resp =
                new DashboardConfigResponse(2L, puller, watchers, "수집 중");

        when(agentConfigService.getConfigsByFolder(1L))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/folders/{folderId}/dashboards/configs", 1L)
                        .requestAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("대시보드 고급설정 조회 성공"))
                .andExpect(jsonPath("$.data[0].dashboardId").value(2L))
                .andExpect(jsonPath("$.data[0].dashboardStatus").value("수집 중"));
    }
}
