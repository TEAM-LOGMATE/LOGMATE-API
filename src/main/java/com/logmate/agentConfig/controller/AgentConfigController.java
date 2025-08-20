package com.logmate.agentConfig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.ConfigDTO;
import com.logmate.agentConfig.model.AgentConfiguration;
import com.logmate.agentConfig.repository.AgentConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentConfigController {

    private final AgentConfigurationRepository repository;
    private final ObjectMapper objectMapper;

    @GetMapping("/config")
    public ResponseEntity<?> getConfig(
            @RequestParam String agentId,
            @RequestParam String eTag,
            @RequestHeader("Authorization") String token // 토큰 검증 로직 따로
    ) {
        AgentConfiguration config = repository.findTopByAgentIdOrderByCreatedAtDesc(agentId)
                .orElse(null);

        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agent 설정 없음");
        }

        if (config.getEtag().equals(eTag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build(); // 304
        }

        try {
            ConfigDTO dto = objectMapper.readValue(config.getConfigJson(), ConfigDTO.class);
            return ResponseEntity.ok(dto); // 200 OK + JSON
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Config 파싱 실패");
        }
    }
}

