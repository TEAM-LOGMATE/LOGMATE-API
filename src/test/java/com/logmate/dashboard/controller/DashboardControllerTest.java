package com.logmate.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.dashboard.dto.DashboardDto;
import com.logmate.dashboard.dto.DashboardRequest;
import com.logmate.dashboard.service.DashboardService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @InjectMocks
    DashboardController dashboardController;

    @Mock
    DashboardService dashboardService;

    @Mock
    UserRepository userRepository;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .build();
    }

    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private DashboardDto createDashboardDto(Long id, String name, String logPath, String agentId, Integer thNum) {
        return new DashboardDto(
                id,
                name,
                logPath,
                null,
                1L,
                agentId,
                thNum
        );
    }

    @Test
    @DisplayName("GET /api/folders/{folderId}/dashboards - 대시보드 목록 조회 성공")
    void getDashboards_success() throws Exception {
        Long folderId = 1L;
        String email = "test@test.com";
        User requester = createUser(1L, email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(requester));

        DashboardDto dto1 = createDashboardDto(1L, "대시보드1", "/var/log/app1.log", "agent-1", 1);
        DashboardDto dto2 = createDashboardDto(2L, "대시보드2", "/var/log/app2.log", "agent-2", 2);

        when(dashboardService.getDashboardsByFolder(folderId, requester))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/folders/{folderId}/dashboards", folderId)
                        .requestAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("대시보드 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].name").value("대시보드1"))
                .andExpect(jsonPath("$.data[0].logPath").value("/var/log/app1.log"))
                .andExpect(jsonPath("$.data[0].agentId").value("agent-1"))
                .andExpect(jsonPath("$.data[0].thNum").value(1));
    }

    @Test
    @DisplayName("POST /api/folders/{folderId}/dashboards - 대시보드 생성 성공")
    void createDashboard_success() throws Exception {
        Long folderId = 1L;
        String email = "test@test.com";
        User requester = createUser(1L, email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(requester));

        DashboardRequest request = new DashboardRequest();
        request.setName("새 대시보드");
        request.setLogPath("/var/log/new.log");

        DashboardDto created = createDashboardDto(10L, "새 대시보드", "/var/log/new.log", null, null);
        when(dashboardService.createDashboard(eq(folderId), any(DashboardRequest.class), eq(requester)))
                .thenReturn(created);

        mockMvc.perform(post("/api/folders/{folderId}/dashboards", folderId)
                        .requestAttr("email", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("대시보드 생성 성공"))
                .andExpect(jsonPath("$.data.name").value("새 대시보드"))
                .andExpect(jsonPath("$.data.logPath").value("/var/log/new.log"));
    }

    @Test
    @DisplayName("PUT /api/folders/{folderId}/dashboards/{dashboardId} - 대시보드 수정 성공")
    void updateDashboard_success() throws Exception {
        Long folderId = 1L;
        Long dashboardId = 10L;
        String email = "test@test.com";
        User requester = createUser(1L, email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(requester));

        DashboardRequest request = new DashboardRequest();
        request.setName("수정된 대시보드");
        request.setLogPath("/var/log/edited.log");

        DashboardDto updated = createDashboardDto(dashboardId, "수정된 대시보드", "/var/log/edited.log", "agent-1", 1);
        when(dashboardService.updateDashboard(eq(folderId), eq(dashboardId), any(DashboardRequest.class), eq(requester)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/folders/{folderId}/dashboards/{dashboardId}", folderId, dashboardId)
                        .requestAttr("email", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("대시보드 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("수정된 대시보드"))
                .andExpect(jsonPath("$.data.logPath").value("/var/log/edited.log"));
    }

    @Test
    @DisplayName("DELETE /api/folders/{folderId}/dashboards/{dashboardId} - 대시보드 삭제 성공")
    void deleteDashboard_success() throws Exception {
        Long folderId = 1L;
        Long dashboardId = 10L;
        String email = "test@test.com";
        User requester = createUser(1L, email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(requester));

        mockMvc.perform(delete("/api/folders/{folderId}/dashboards/{dashboardId}", folderId, dashboardId)
                        .requestAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("대시보드 삭제 성공"));
    }
}
