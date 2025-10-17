package com.logmate.dashboard.service;

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
import com.logmate.team.repository.TeamMemberRepository;
import com.logmate.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final FolderRepository folderRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final AgentConfigurationRepository  agentConfigurationRepository;
    private final LogPipelineConfigRepository logPipelineConfigRepository;


    public List<DashboardDto> getDashboardsByFolder(Long folderId, User requester) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 폴더입니다."));

        assertFolderReadable(folder, requester);
        return dashboardRepository.findByFolderId(folderId).stream()
                .map(dashboard -> {
                    //dashboardId 기준으로 agentId 조회
                    String agentId = agentConfigurationRepository.findByDashboardId(dashboard.getId())
                            .map(agentConfig -> agentConfig.getAgentId())
                            .orElse(null); // 없으면 null

                    Integer thNum = logPipelineConfigRepository.findByDashboardId(dashboard.getId())
                            .stream()
                            .findFirst()
                            .map(LogPipelineConfig::getThNum)
                            .orElse(null);

                    return DashboardDto.from(dashboard, agentId, thNum);
                })
                .toList();
    }

    public DashboardDto createDashboard(Long folderId, DashboardRequest request, User requester) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 폴더입니다."));

        assertFolderWritable(folder, requester);

        Dashboard dashboard = Dashboard.builder()
                .name(request.getName())
                .logPath(request.getLogPath())
                .folder(folder)
                .team(folder.getTeam()) // 팀 폴더인 경우
                .user(folder.getUser()) // 개인 폴더인 경우
                .build();

        dashboardRepository.save(dashboard);
        return DashboardDto.from(dashboard, null);
    }

    public DashboardDto updateDashboard(Long folderId, Long dashboardId, DashboardRequest request, User requester) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "대시보드를 찾을 수 없습니다."));

        if (!dashboard.getFolder().getId().equals(folderId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 폴더에 속한 대시보드가 아닙니다.");
        }

        assertFolderWritable(dashboard.getFolder(), requester);

        dashboard.setName(request.getName());
        dashboard.setLogPath(request.getLogPath());

        dashboardRepository.save(dashboard);
        String agentId = agentConfigurationRepository.findByDashboardId(dashboardId)
                .map(agentConfig -> agentConfig.getAgentId())
                .orElse(null);

        return DashboardDto.from(dashboard);
    }

    @Transactional
    public void deleteDashboard(Long folderId, Long dashboardId, User requester) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "대시보드를 찾을 수 없습니다."));

        if (!dashboard.getFolder().getId().equals(folderId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 폴더에 속한 대시보드가 아닙니다.");
        }
        assertFolderWritable(dashboard.getFolder(), requester);
        dashboardRepository.delete(dashboard);
    }

    //권한 체크
    private void assertFolderReadable(Folder folder, User requester) {
        assertFolderWritable(folder, requester);
    }
    private void assertFolderWritable(Folder folder, User requester) {
        if (folder.getUser() != null) { // 개인 폴더
            if (!folder.getUser().getId().equals(requester.getId())) {
                throw new CustomException(HttpStatus.FORBIDDEN, "개인 폴더 접근 권한이 없습니다.");
            }
            return;
        }
        if (folder.getTeam() != null) { // 팀 폴더
            boolean isMember = teamMemberRepository
                    .findByUserIdAndTeamId(requester.getId(), folder.getTeam().getId())
                    .isPresent();
            if (!isMember) {
                throw new CustomException(HttpStatus.FORBIDDEN, "팀 폴더 접근 권한이 없습니다.");
            }
            return;
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "잘못된 폴더입니다.");
    }

    public List<Long> getDashboardIdsByFolder(Long folderId) {
        return dashboardRepository.findIdsByFolderId(folderId);
    }
}
