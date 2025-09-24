package com.logmate.agentConfig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.*;
import com.logmate.agentConfig.model.AgentConfiguration;
import com.logmate.agentConfig.model.LogPipelineConfig;
import com.logmate.agentConfig.repository.AgentConfigurationRepository;
import com.logmate.agentConfig.repository.LogPipelineConfigRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AgentConfigService {

    private final AgentConfigurationRepository repository;
    private final LogPipelineConfigRepository logPipelineRepository;
    private final ObjectMapper objectMapper;

    public String saveConfig(SaveDashboardConfigRequest request) {
        try {
            //String etag = UUID.randomUUID().toString(); // 새로운 etag 생성
            String configEtag = UUID.randomUUID().toString();

            String agentId = request.getAgentId();
            AgentConfiguration entity;

            if (agentId == null || agentId.isBlank()) {
                agentId = UUID.randomUUID().toString();
                entity = new AgentConfiguration(agentId, configEtag, "{}");
                repository.save(entity);
            } else {
                entity = repository.findByAgentId(agentId)
                        .orElse(new AgentConfiguration(agentId, configEtag, "{}"));

                repository.save(entity);
            }
            // AgentConfig
            AgentConfig agentConfig = new AgentConfig();
            agentConfig.setAgentId(agentId);
            agentConfig.setAccessToken("generated-access-token");
            agentConfig.setEtag(UUID.randomUUID().toString());

            //PullerConfig
            PullerConfig pullerConfig = new PullerConfig();
            pullerConfig.setPullURL("http://15.164.114.73/api/config"); // agnet pull 요청 URL
            pullerConfig.setIntervalSec(0);
            pullerConfig.setEtag(UUID.randomUUID().toString());

            int currentCount = logPipelineRepository.findByAgentConfiguration(entity).size();
            int thNum = currentCount + 1;

            //WatcherConfig
            List<WatcherConfig> watcherConfigs = new ArrayList<>();
            for (SaveDashboardConfigRequest.WatcherRequest wReq : request.getLogPipelineConfigs()) {
                WatcherConfig watcher = new WatcherConfig();
                watcher.setEtag(UUID.randomUUID().toString());
                watcher.setThNum(thNum++);

                // Tailer
                TailerConfig tailer = new TailerConfig();
                tailer.setFilePath(wReq.getTailer() != null ? wReq.getTailer().getFilePath() : null);
                tailer.setReadIntervalMs(wReq.getTailer() != null ? wReq.getTailer().getReadIntervalMs() : 0);
                tailer.setMetaDataFilePathPrefix(wReq.getTailer() != null ? wReq.getTailer().getMetaDataFilePathPrefix() : null);
                watcher.setTailer(tailer);

                // Multiline
                MultilineConfig multiline = new MultilineConfig();
                multiline.setEnabled(wReq.getMultiline() != null && wReq.getMultiline().isEnabled());
                multiline.setMaxLines(wReq.getMultiline() != null ? wReq.getMultiline().getMaxLines() : 0);
                watcher.setMultiline(multiline);

                // Exporter
                ExporterConfig exporter = new ExporterConfig();
                String pushUrl = String.format(
                        "http://ec2-3-39-232-72.ap-northeast-2.compute.amazonaws.com:8080/api/v1/streaming/logs/tomcat/%s/%d",
                        agentId,
                        watcher.getThNum()
                );
                exporter.setCompressEnabled(wReq.getExporter() != null ? wReq.getExporter().getCompressEnabled() : null);
                exporter.setRetryIntervalSec(wReq.getExporter() != null ? wReq.getExporter().getRetryIntervalSec() : 0);
                exporter.setMaxRetryCount(wReq.getExporter() != null ? wReq.getExporter().getMaxRetryCount() : 0);
                watcher.setExporter(exporter);

                // Parser
                ParserConfig parser = new ParserConfig();
                parser.setType(wReq.getParserType());

                if (wReq.getParser() != null) {
                    ParserConfig.ParserDetailConfig detail = new ParserConfig.ParserDetailConfig();
                    detail.setTimezone(wReq.getParser().getTimezone());
                    parser.setConfig(detail);
                } else {
                    parser.setConfig(null);
                }
                watcher.setParser(parser);

                // Filter
                FilterConfig filter = new FilterConfig();
                if ("tomcat".equalsIgnoreCase(wReq.getParserType())) {
                    filter.setAllowedMethods(wReq.getFilter() != null ? Set.copyOf(wReq.getFilter().getAllowedMethods()) : Set.of());
                } else { // springboot
                    filter.setAllowedLevels(wReq.getFilter() != null ? Set.copyOf(wReq.getFilter().getAllowedLevels()) : Set.of());
                    filter.setRequiredKeywords(wReq.getFilter() != null ? Set.copyOf(wReq.getFilter().getRequiredKeywords()) : Set.of());
                }
                watcher.setFilter(filter);

                watcherConfigs.add(watcher);

                // DB에 logPipeline 저장
                String wcJson = objectMapper.writeValueAsString(watcher);
                logPipelineRepository.save(
                        new LogPipelineConfig(
                                watcher.getEtag(),
                                watcher.getThNum(),
                                watcher.getTailer() != null ? watcher.getTailer().getFilePath() : null,
                                wcJson,
                                entity
                        )
                );
            }

            List<LogPipelineConfig> pipelines = logPipelineRepository.findByAgentConfiguration(entity);
            List<WatcherConfig> allWatchers = new ArrayList<>();
            for (LogPipelineConfig p : pipelines) {
                WatcherConfig wc = objectMapper.readValue(p.getConfigJson(), WatcherConfig.class);
                String pushUrl = String.format(
                        "http://ec2-3-39-232-72.ap-northeast-2.compute.amazonaws.com:8080/api/v1/streaming/logs/tomcat/%s/%d",
                        agentId,
                        wc.getThNum()
                );
                if (wc.getExporter() != null) {
                    wc.getExporter().setPushURL(pushUrl);
                }
                allWatchers.add(wc);
            }

            // 최종 ConfigDTO
            ConfigDTO configDTO = new ConfigDTO();
            configDTO.setEtag(configEtag);
            configDTO.setAgentConfig(agentConfig);
            configDTO.setPullerConfig(pullerConfig);
            configDTO.setLogPipelineConfigs(allWatchers);

            // 저장
            String json = objectMapper.writeValueAsString(configDTO);
            entity.update(configEtag, json);
            repository.save(entity);
            return agentId;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Config 저장 실패", e);
        }
    }

    public ConfigDTO getConfig(String agentId, String etag) {
        return repository.findByAgentId(agentId)
                .map(config -> {
                    if (etag != null && !etag.isBlank() && config.getEtag().equals(etag)) {
                        return null; // 변경 없음
                    }
                    try {
                        ConfigDTO dto = objectMapper.readValue(config.getConfigJson(), ConfigDTO.class);

                        // logPipelineConfig DB에 저장된 항목들 다시 조립
                        List<LogPipelineConfig> pipelineEntities = logPipelineRepository.findByAgentConfiguration(config);
                        List<WatcherConfig> watchers = new ArrayList<>();
                        for (LogPipelineConfig p : pipelineEntities) {
                            WatcherConfig wc = objectMapper.readValue(p.getConfigJson(), WatcherConfig.class);
                            watchers.add(wc);
                        }
                        // intervalSec 계산
                        if (config.getLastUpdatedAt() != null) {
                            long seconds = Duration.between(config.getLastUpdatedAt(), LocalDateTime.now()).getSeconds();
                            PullerConfig puller = dto.getPullerConfig();
                            if (puller == null) {
                                puller = new PullerConfig();
                            }
                            puller.setIntervalSec((int) seconds);
                            puller.setEtag(UUID.randomUUID().toString());
                            dto.setPullerConfig(puller);
                        }
                        return dto;
                    } catch (Exception e) {
                        throw new RuntimeException("Config 파싱 실패", e);
                    }
                })
                .orElse(null); // agentId 설정 없음
    }
}