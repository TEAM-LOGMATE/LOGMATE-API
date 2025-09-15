package com.logmate.agentConfig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.*;
import com.logmate.agentConfig.repository.AgentConfigurationRepository;
import com.logmate.agentConfig.model.AgentConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentConfigService {

    private final AgentConfigurationRepository repository;
    private final ObjectMapper objectMapper;

    public String  saveConfig(SaveDashboardConfigRequest request) {
        try {
            String etag = UUID.randomUUID().toString(); // 새로운 etag 생성
            String agentId = UUID.randomUUID().toString();

            PullerConfig pullerConfig = new PullerConfig();
            pullerConfig.setPullURL("https://api.logmate.io/api/agents/config"); // 서버 고정값
            pullerConfig.setIntervalSec(request.getPuller().getIntervalSec());
            pullerConfig.setEtag(etag);

            // WatcherConfig 구성
            WatcherConfig watcherConfig = new WatcherConfig();
            watcherConfig.setEtag(etag);
            watcherConfig.setThNum(1);

            TailerConfig tailer = new TailerConfig();
            tailer.setReadIntervalMs(request.getTailer().getReadIntervalMs());
            tailer.setMetaDataFilePathPrefix(request.getTailer().getMetaDataFilePathPrefix());
            tailer.setFilePath("/var/log/app.log"); // 서버에서 기본 제공
            watcherConfig.setTailer(tailer);

            MultilineConfig multiline = new MultilineConfig();
            multiline.setEnabled(request.getMultiline().isEnabled());
            multiline.setMaxLines(request.getMultiline().getMaxLines());
            watcherConfig.setMultiline(multiline);

            ExporterConfig exporter = new ExporterConfig();
            exporter.setPushURL("https://stream.logmate.io/ingest"); // 서버에서 제공
            exporter.setCompressEnabled(request.getExporter().getCompressEnabled());
            exporter.setRetryIntervalSec(request.getExporter().getRetryIntervalSec());
            exporter.setMaxRetryCount(request.getExporter().getMaxRetryCount());
            watcherConfig.setExporter(exporter);

            ParserConfig parser = new ParserConfig();
            parser.setType("springboot"); // 서버에서 기본 제공
            parser.setConfig(new ParserConfig.ParserDetailConfig("yyyy-MM-dd HH:mm:ss", "Asia/Seoul"));
            watcherConfig.setParser(parser);

            FilterConfig filter = new FilterConfig();
            filter.setAllowedLevels(Set.copyOf(request.getFilter().getAllowedLevels()));
            filter.setRequiredKeywords(Set.copyOf(request.getFilter().getRequiredKeywords()));
            filter.setAfter(request.getFilter().getAfter());
            filter.setAllowedLoggers(Set.of()); // 화면에는 없음 → 빈 값
            watcherConfig.setFilter(filter);

            // 최종 ConfigDTO
            ConfigDTO configDTO = new ConfigDTO();
            configDTO.setEtag(etag);

            AgentConfig agentConfig = new AgentConfig();
            agentConfig.setAgentId(agentId);
            agentConfig.setAccessToken("generated-access-token"); // 서버 발급
            agentConfig.setEtag(etag);
            configDTO.setAgentConfig(agentConfig);

            configDTO.setPullerConfig(pullerConfig);
            configDTO.setWatcherConfigs(List.of(watcherConfig));

            // JSON 변환 후 DB 저장
            String json = objectMapper.writeValueAsString(configDTO);

            AgentConfiguration entity = repository.findByAgentId(agentId)
                    .map(existing -> {
                        existing.update(etag, json);
                        return existing;
                    })
                    .orElse(new AgentConfiguration(agentId, etag, json));


            repository.save(entity);
            return agentId;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Config 저장 실패", e);
        }
    }

    public ConfigDTO getConfig(String agentId, String etag) {
        return repository.findByAgentId(agentId)
                .map(config -> {
                    if (config.getEtag().equals(etag)) {
                        return null; // 변경 없음
                    }
                    try {
                        return objectMapper.readValue(config.getConfigJson(), ConfigDTO.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Config 파싱 실패", e);
                    }
                })
                .orElse(null); // agentId 설정 없음
    }
}