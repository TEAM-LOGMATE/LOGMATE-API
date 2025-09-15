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

            //PullerConfig
            PullerConfig pullerConfig = new PullerConfig();
            pullerConfig.setPullURL(null); // 서버 고정값 = null
            pullerConfig.setIntervalSec(request.getPuller() != null ? request.getPuller().getIntervalSec() : 0);
            pullerConfig.setEtag(etag);

            //WatcherConfig
            WatcherConfig watcherConfig = new WatcherConfig();
            watcherConfig.setEtag(etag);
            watcherConfig.setThNum(1);

            //TailerConfig
            TailerConfig tailer = new TailerConfig();
            tailer.setReadIntervalMs(request.getTailer() != null ? request.getTailer().getReadIntervalMs() : 0);
            tailer.setMetaDataFilePathPrefix(request.getTailer() != null? request.getTailer().getMetaDataFilePathPrefix() : null);
            tailer.setFilePath(null); // 서버 고정값 = null
            watcherConfig.setTailer(tailer);

            //MultilineConfig
            MultilineConfig multiline = new MultilineConfig();
            multiline.setEnabled(request.getMultiline() != null && request.getMultiline().isEnabled());
            multiline.setMaxLines(request.getMultiline() != null ? request.getMultiline().getMaxLines() : 0);
            watcherConfig.setMultiline(multiline);

            //ExporterConfig
            ExporterConfig exporter = new ExporterConfig();
            exporter.setPushURL(null);  // 서버에서 제공
            exporter.setCompressEnabled(request.getExporter() != null ? request.getExporter().getCompressEnabled() : null);
            exporter.setRetryIntervalSec(request.getExporter() != null ? request.getExporter().getRetryIntervalSec() : 0);
            exporter.setMaxRetryCount(request.getExporter() != null ? request.getExporter().getMaxRetryCount() : 0);
            watcherConfig.setExporter(exporter);

            //ParserConfig
            ParserConfig parser = new ParserConfig();
            parser.setType(null); // 서버 고정값 = null
            parser.setConfig(null); // 서버 고정값 = null
            watcherConfig.setParser(parser);

            //FilterConfig
            FilterConfig filter = new FilterConfig();
            filter.setAllowedLevels(request.getFilter() != null ? Set.copyOf(request.getFilter().getAllowedLevels()) : null);
            filter.setRequiredKeywords(request.getFilter() != null ? Set.copyOf(request.getFilter().getRequiredKeywords()) : null);
            filter.setAfter(request.getFilter() != null ? request.getFilter().getAfter() : null);
            filter.setAllowedLoggers(null); // 화면에는 없음 → 빈 값
            watcherConfig.setFilter(filter);

            // 최종 ConfigDTO
            ConfigDTO configDTO = new ConfigDTO();
            configDTO.setEtag(etag);

            AgentConfig agentConfig = new AgentConfig();
            agentConfig.setAgentId(agentId);
            agentConfig.setAccessToken(null); // 서버 고정값 = null
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