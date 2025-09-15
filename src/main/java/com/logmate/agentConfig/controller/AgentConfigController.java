package com.logmate.agentConfig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.ConfigDTO;
import com.logmate.agentConfig.dto.SaveDashboardConfigRequest;
import com.logmate.agentConfig.repository.AgentConfigurationRepository;
import com.logmate.agentConfig.service.AgentConfigService;
import com.logmate.global.BaseResponse;
import com.logmate.global.CustomException;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/folders/{folderId}/dashboards")
@RequiredArgsConstructor
public class AgentConfigController {
    private final AgentConfigService service;
    private final UserRepository userRepository;

    private final AgentConfigurationRepository repository;
    private final ObjectMapper objectMapper;

    @PostMapping("/{dashboardId}/config")
    public ResponseEntity<BaseResponse<Map<String, String>>> saveConfig(@PathVariable Long folderId,
                                                         @PathVariable Long dashboardId,
                                                         @RequestBody SaveDashboardConfigRequest request,
                                                         HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

//        service.saveConfig("agent-" + dashboardId, request);
        String agentId = service.saveConfig(request);

        Map<String, String> response = Map.of("agentId", agentId);
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
}

