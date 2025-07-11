package com.logmate.dashboard.service;

import com.logmate.dashboard.dto.DashboardRequest;
import com.logmate.dashboard.dto.DashboardDto;
import com.logmate.dashboard.model.Dashboard;
import com.logmate.dashboard.repository.DashboardRepository;
import com.logmate.team.model.Team;
import com.logmate.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final TeamRepository teamRepository;

    public List<DashboardDto> getDashboardsByTeam(Long teamId) {
        return dashboardRepository.findByTeamId(teamId).stream()
                .map(DashboardDto::from)
                .toList();
    }

    public DashboardDto createDashboard(Long teamId, DashboardRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        Dashboard dashboard = Dashboard.builder()
                .name(request.getName())
                .logPath(request.getLogPath())
                .sendUrl(request.getSendTo())
                .team(team)
                .build();

        dashboardRepository.save(dashboard);

        return DashboardDto.from(dashboard);
    }

    public DashboardDto updateDashboard(Long teamId, Long dashboardId, DashboardRequest request) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new IllegalArgumentException("대시보드를 찾을 수 없습니다."));

        if (!dashboard.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("해당 팀에 속한 대시보드가 아닙니다.");
        }

        dashboard.setName(request.getName());
        dashboard.setLogPath(request.getLogPath());
        dashboard.setSendUrl(request.getSendTo());

        dashboardRepository.save(dashboard);

        return DashboardDto.from(dashboard);
    }
}
