package com.logmate.agentConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.ConfigDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentConfigService {

    private final AgentConfigurationRepository repository;
    private final ObjectMapper objectMapper;

    public void saveConfig(ConfigDTO configDTO) {
        try {
            String json = objectMapper.writeValueAsString(configDTO);
            String etag = UUID.randomUUID().toString(); // 새로운 etag 생성

            AgentConfiguration config = new AgentConfiguration(
                    configDTO.getAgentConfig().getAgentId(),
                    etag,
                    json
            );

            repository.save(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Config 저장 실패", e);
        }
    }
}