package com.logmate.agentConfig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.ConfigDTO;
import com.logmate.agentConfig.dto.DashboardConfigResponse;
import com.logmate.agentConfig.dto.SaveDashboardConfigRequest;
import com.logmate.agentConfig.service.AgentConfigService;
import com.logmate.global.BaseResponse;
import com.logmate.global.CustomException;
import com.logmate.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AgentConfigController {
    private final AgentConfigService service;
    private final UserRepository userRepository;

    @PostMapping("/folders/{folderId}/dashboards/{dashboardId}/config")
    public ResponseEntity<BaseResponse<Map<String, Object>>> saveConfig(@PathVariable Long folderId,
                                                         @PathVariable Long dashboardId,
                                                         @RequestBody SaveDashboardConfigRequest request,
                                                         HttpServletRequest httpRequest) {
        String agentId = service.saveConfig(request, dashboardId);

        Map<String, Object> response = Map.of(
                "agentId", agentId,
                "logpipelineConfigs", request.getLogPipelineConfigs()
                );
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 고급설정 저장 성공", response));
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig(@RequestParam String agentId,
                                       @RequestParam String etag,
                                       HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        ConfigDTO config = service.getConfig(agentId, etag);

        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(BaseResponse.of(304, "변경된 설정 없음", null));
        }
        return ResponseEntity.ok(config); // 최신 ConfigDTO 응답
    }

    @PutMapping("/folders/{folderId}/dashboards/{dashboardId}/config")
    public ResponseEntity<BaseResponse<Map<String, Object>>> updatePipeline(@PathVariable Long folderId,
                                                                            @PathVariable Long dashboardId,
                                                                            @RequestBody Map<String, Object> requestBody,
                                                                            HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 요청 파라미터 파싱
        String agentId = (String) requestBody.get("agentId");
        String targetFilePath = (String) requestBody.get("targetFilePath");

        // Jackson 으로 nested object → DTO 변환
        SaveDashboardConfigRequest.WatcherRequest watcherReq =
                new ObjectMapper().convertValue(requestBody.get("logPipelineConfig"),
                        SaveDashboardConfigRequest.WatcherRequest.class);

        SaveDashboardConfigRequest.PullerRequest pullerReq =
                new ObjectMapper().convertValue(requestBody.get("puller"),
                        SaveDashboardConfigRequest.PullerRequest.class);


        service.updatePipeline(agentId, targetFilePath, dashboardId, watcherReq, pullerReq);

        Map<String, Object> response = Map.of(
                "agentId", agentId,
                "updatedFilePath", targetFilePath,
                "logpipelineConfigs", watcherReq
        );

        return ResponseEntity.ok(BaseResponse.of(200, "LogPipeline 업데이트 성공", response));
    }
    @GetMapping("/folders/{folderId}/dashboards/configs")
    public ResponseEntity<BaseResponse<List<DashboardConfigResponse>>> getConfigsByFolder(
            @PathVariable Long folderId,
            HttpServletRequest httpRequest) {

        String email = (String) httpRequest.getAttribute("email");
        userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<DashboardConfigResponse> configs = service.getConfigsByFolder(folderId);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 고급설정 조회 성공", configs));
    }
}

