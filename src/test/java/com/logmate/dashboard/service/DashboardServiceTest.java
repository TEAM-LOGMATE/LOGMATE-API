package com.logmate.dashboard.service;

import com.logmate.agentConfig.model.AgentConfiguration;
import com.logmate.agentConfig.model.LogPipelineConfig;
import com.logmate.agentConfig.repository.AgentConfigurationRepository;
import com.logmate.agentConfig.repository.LogPipelineConfigRepository;
import com.logmate.dashboard.dto.DashboardDto;
import com.logmate.dashboard.dto.DashboardRequest;
import com.logmate.dashboard.model.Dashboard;
import com.logmate.dashboard.repository.DashboardRepository;
import com.logmate.folder.model.Folder;
import com.logmate.folder.repository.FolderRepository;
import com.logmate.global.CustomException;
import com.logmate.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    DashboardRepository dashboardRepository;

    @Mock
    FolderRepository folderRepository;

    @Mock
    AgentConfigurationRepository agentConfigurationRepository;

    @Mock
    LogPipelineConfigRepository logPipelineConfigRepository;

    @InjectMocks
    DashboardService dashboardService;

    @Test
    @DisplayName("getDashboardsByFolder - 개인 폴더 + 본인 요청이면 대시보드 목록 조회 성공")
    void getDashboardsByFolder_personalFolder_success() {
        // given
        Long folderId = 1L;
        User owner = new User();
        owner.setId(10L);

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setUser(owner); // 개인 폴더

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folder));

        Dashboard dash = Dashboard.builder()
                .id(100L)
                .name("에러로그 대시보드")
                .logPath("/var/log/app.log")
                .folder(folder)
                .build();

        when(dashboardRepository.findByFolderId(folderId))
                .thenReturn(List.of(dash));

        // agentConfig, pipeline 은 anyLong 으로 대충 묶어서 스텁
        AgentConfiguration agentConfig = new AgentConfiguration("agent-1", "etag-1", "{}");
        when(agentConfigurationRepository.findByDashboardId(anyLong()))
                .thenReturn(Optional.of(agentConfig));

        LogPipelineConfig pipeline =
                new LogPipelineConfig("wc-etag", 1, "/var/log/app.log", "{}", agentConfig, 100L);
        when(logPipelineConfigRepository.findByDashboardId(anyLong()))
                .thenReturn(List.of(pipeline));

        // when
        List<DashboardDto> result = dashboardService.getDashboardsByFolder(folderId, owner);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(folderRepository).findById(folderId);
        verify(dashboardRepository).findByFolderId(folderId);
    }

    @Test
    @DisplayName("getDashboardsByFolder - 존재하지 않는 폴더면 404")
    void getDashboardsByFolder_folderNotFound_throws() {
        // given
        Long folderId = 1L;
        User requester = new User();
        requester.setId(10L);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dashboardService.getDashboardsByFolder(folderId, requester))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 폴더입니다.");
    }

    @Test
    @DisplayName("getDashboardsByFolder - 개인 폴더인데 다른 사용자가 접근하면 403")
    void getDashboardsByFolder_personalFolder_forbidden() {
        // given
        Long folderId = 1L;

        User owner = new User();
        owner.setId(10L);

        User other = new User();
        other.setId(99L);

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setUser(owner); // 개인 폴더

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folder));

        // when & then
        assertThatThrownBy(() -> dashboardService.getDashboardsByFolder(folderId, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("개인 폴더 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("createDashboard - 폴더 쓰기 권한 있으면 대시보드 생성 성공")
    void createDashboard_success() {
        // given
        Long folderId = 1L;

        User owner = new User();
        owner.setId(10L);

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setUser(owner);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folder));

        DashboardRequest request = new DashboardRequest();
        request.setName("새 대시보드");
        request.setLogPath("/var/log/new.log");

        Dashboard saved = Dashboard.builder()
                .id(123L)
                .name(request.getName())
                .logPath(request.getLogPath())
                .folder(folder)
                .build();

        when(dashboardRepository.save(any(Dashboard.class)))
                .thenReturn(saved);

        // when
        DashboardDto result = dashboardService.createDashboard(folderId, request, owner);

        // then
        assertThat(result).isNotNull();
        verify(dashboardRepository).save(any(Dashboard.class));
    }

    @Test
    @DisplayName("deleteDashboard - 폴더/대시보드 일치 + 권한 OK면 삭제 성공")
    void deleteDashboard_success() {
        // given
        Long folderId = 1L;
        Long dashboardId = 10L;

        User owner = new User();
        owner.setId(10L);

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setUser(owner);

        Dashboard dash = Dashboard.builder()
                .id(dashboardId)
                .name("삭제 대상")
                .logPath("/var/log/app.log")
                .folder(folder)
                .build();

        when(dashboardRepository.findById(dashboardId))
                .thenReturn(Optional.of(dash));

        // when
        dashboardService.deleteDashboard(folderId, dashboardId, owner);

        // then
        verify(dashboardRepository).delete(dash);
    }

    @Test
    @DisplayName("deleteDashboard - 다른 폴더에 속한 대시보드면 400")
    void deleteDashboard_wrongFolder_throws() {
        // given
        Long folderId = 1L;
        Long otherFolderId = 2L;
        Long dashboardId = 10L;

        User owner = new User();
        owner.setId(10L);

        Folder folder = new Folder();
        folder.setId(otherFolderId); // 폴더 ID 다르게

        Dashboard dash = Dashboard.builder()
                .id(dashboardId)
                .name("삭제 대상")
                .logPath("/var/log/app.log")
                .folder(folder)
                .build();

        when(dashboardRepository.findById(dashboardId))
                .thenReturn(Optional.of(dash));

        // when & then
        assertThatThrownBy(() -> dashboardService.deleteDashboard(folderId, dashboardId, owner))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 폴더에 속한 대시보드가 아닙니다.");
    }
}
