package com.logmate.dashboard.service;

import com.logmate.agentConfig.dto.*;
import com.logmate.agentConfig.service.AgentConfigService;
import com.logmate.dashboard.dto.DashboardRequest;
import com.logmate.dashboard.dto.DashboardDto;
import com.logmate.dashboard.model.Dashboard;
import com.logmate.dashboard.repository.DashboardRepository;
import com.logmate.folder.model.Folder;
import com.logmate.folder.repository.FolderRepository;
import com.logmate.global.CustomException;
import com.logmate.team.model.Team;
import com.logmate.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final TeamRepository teamRepository;
    private final FolderRepository folderRepository;
    private final AgentConfigService agentConfigService;

    public List<DashboardDto> getDashboardsByFolder(Long folderId) {
        return dashboardRepository.findByFolderId(folderId).stream()
                .map(DashboardDto::from)
                .toList();
    }

    public DashboardDto createDashboard(Long folderId, DashboardRequest request) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 폴더입니다."));

        Dashboard dashboard = Dashboard.builder()
                .name(request.getName())
                .logPath(request.getLogPath())
                .sendUrl(request.getSendTo())
                .folder(folder)
                .team(folder.getTeam()) // 팀 폴더인 경우
                .user(folder.getUser()) // 개인 폴더인 경우
                .build();

        dashboardRepository.save(dashboard);

        //TODO 팀-폴더 관계 명확히 수정한 후에 이부분 수정
        //ConfigDTO configDTO = makeConfigFromDashboard(dashboard, team);
        //agentConfigService.saveConfig(configDTO);

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

    private ConfigDTO makeConfigFromDashboard(Dashboard dashboard, Team team) {
        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setAgentId("agent-" + team.getId()); // 팀 ID 기반 식별자
        agentConfig.setAccessToken(null); // 아직 발급 안했으면 null 또는 "" 처리
        agentConfig.setEtag(UUID.randomUUID().toString());

        PullerConfig pullerConfig = new PullerConfig();
        pullerConfig.setPullURL("http://localhost:8080/api/agents/config");
        pullerConfig.setIntervalSec(30); // 기본값
        pullerConfig.setEtag(UUID.randomUUID().toString());

        WatcherConfig watcher = new WatcherConfig();
        watcher.setEtag(UUID.randomUUID().toString());
        watcher.setThNum(1); //기본값 1스레드

        TailerConfig tailer = new TailerConfig();
        tailer.setFilePath(dashboard.getLogPath());
        tailer.setReadIntervalMs(1000);
        tailer.setMetaDataFilePathPrefix("/var/log/meta"); // 고정값

        watcher.setTailer(tailer);

        MultilineConfig multiline = new MultilineConfig();
        multiline.setEnabled(true);
        multiline.setMaxLines(10);
        watcher.setMultiline(multiline);

        ExporterConfig exporter = new ExporterConfig();
        exporter.setPushURL(dashboard.getSendUrl()); // 대시보드에 등록된 전송 URL
        exporter.setCompressEnabled(true);
        exporter.setRetryIntervalSec(5);
        exporter.setMaxRetryCount(3);
        watcher.setExporter(exporter);

        ParserDetailConfig parserDetail = new ParserDetailConfig("yyyy-MM-dd HH:mm:ss", "Asia/Seoul");
        ParserConfig parser = new ParserConfig();
        parser.setType("springboot");
        parser.setConfig(parserDetail);
        watcher.setParser(parser);

        FilterConfig filter = new FilterConfig();
        filter.setAllowedLevels(Set.of("ERROR", "WARN"));
        filter.setAllowedLoggers(Set.of());
        filter.setRequiredKeywords(Set.of());
        filter.setAfter(LocalDateTime.now().minusDays(1)); // 최근 하루 기준

        watcher.setFilter(filter);

        // 4. ConfigDTO 조립
        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setEtag("config-dto-etag-" + UUID.randomUUID());
        configDTO.setAgentConfig(agentConfig);
        configDTO.setPullerConfig(pullerConfig);
        configDTO.setWatcherConfigs(List.of(watcher));

        return configDTO;
    }
}
