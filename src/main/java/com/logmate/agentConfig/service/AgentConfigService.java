package com.logmate.agentConfig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmate.agentConfig.dto.*;
import com.logmate.agentConfig.model.AgentConfiguration;
import com.logmate.agentConfig.model.LogPipelineConfig;
import com.logmate.agentConfig.repository.AgentConfigurationRepository;
import com.logmate.agentConfig.repository.LogPipelineConfigRepository;
import com.logmate.dashboard.service.DashboardService;
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

    public String saveConfig(SaveDashboardConfigRequest request, Long dashboardId) {
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
                                entity,
                                dashboardId
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

    @Transactional
    public void updatePipeline(String agentId, String targetFilePath, Long dashboardId, SaveDashboardConfigRequest.WatcherRequest request) {
        AgentConfiguration agentConfig = repository.findByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        LogPipelineConfig pipeline = logPipelineRepository.findByAgentConfigurationAndFilePathAndDashboardId(agentConfig, targetFilePath, dashboardId);
        if (pipeline == null) {
            throw new RuntimeException("Pipeline not found for filePath: " + targetFilePath + " and dashboardId: " + dashboardId);
        }

        try {
            // 새로운 watcherConfig 객체 생성
            WatcherConfig watcher = new WatcherConfig();
            watcher.setEtag(UUID.randomUUID().toString());
            watcher.setThNum(pipeline.getThNum()); // 기존 thNum 유지

            // Tailer
            TailerConfig tailer = new TailerConfig();
            tailer.setFilePath(request.getTailer() != null ? request.getTailer().getFilePath() : null);
            tailer.setReadIntervalMs(request.getTailer() != null ? request.getTailer().getReadIntervalMs() : 0);
            tailer.setMetaDataFilePathPrefix(request.getTailer() != null ? request.getTailer().getMetaDataFilePathPrefix() : null);
            watcher.setTailer(tailer);

            // Multiline
            MultilineConfig multiline = new MultilineConfig();
            multiline.setEnabled(request.getMultiline() != null && request.getMultiline().isEnabled());
            multiline.setMaxLines(request.getMultiline() != null ? request.getMultiline().getMaxLines() : 0);
            watcher.setMultiline(multiline);

            // Exporter
            ExporterConfig exporter = new ExporterConfig();
            exporter.setPushURL(null);
            exporter.setCompressEnabled(request.getExporter() != null ? request.getExporter().getCompressEnabled() : null);
            exporter.setRetryIntervalSec(request.getExporter() != null ? request.getExporter().getRetryIntervalSec() : 0);
            exporter.setMaxRetryCount(request.getExporter() != null ? request.getExporter().getMaxRetryCount() : 0);
            watcher.setExporter(exporter);

            // Parser
            ParserConfig parser = new ParserConfig();
            parser.setType(request.getParserType());

            if (request.getParser() != null) {
                ParserConfig.ParserDetailConfig detail = new ParserConfig.ParserDetailConfig();
                detail.setTimezone(request.getParser().getTimezone());
                parser.setConfig(detail);
            } else {
                parser.setConfig(null);
            }
            watcher.setParser(parser);

            // Filter
            FilterConfig filter = new FilterConfig();
            if ("tomcat".equalsIgnoreCase(request.getParserType())) {
                filter.setAllowedMethods(request.getFilter() != null ? Set.copyOf(request.getFilter().getAllowedMethods()) : Set.of());
            } else {
                filter.setAllowedLevels(request.getFilter() != null ? Set.copyOf(request.getFilter().getAllowedLevels()) : Set.of());
                filter.setRequiredKeywords(request.getFilter() != null ? Set.copyOf(request.getFilter().getRequiredKeywords()) : Set.of());
            }
            watcher.setFilter(filter);

            // JSON 직렬화 후 엔티티 업데이트
            String wcJson = objectMapper.writeValueAsString(watcher);
            pipeline.update(watcher.getEtag(), tailer.getFilePath(), wcJson);

            logPipelineRepository.save(pipeline);

            ConfigDTO dto = objectMapper.readValue(agentConfig.getConfigJson(), ConfigDTO.class);
            // 최신 pipeline 목록 가져오기
            List<LogPipelineConfig> pipelines = logPipelineRepository.findByAgentConfiguration(agentConfig);
            List<WatcherConfig> watchers = new ArrayList<>();
            for (LogPipelineConfig p : pipelines) {
                WatcherConfig wc = objectMapper.readValue(p.getConfigJson(), WatcherConfig.class);
                watchers.add(wc);
            }
            dto.setLogPipelineConfigs(watchers);

            // 전체 etag 갱신
            String configEtag = UUID.randomUUID().toString();
            dto.setEtag(configEtag);

            // 다시 직렬화 후 agentConfig 갱신
            String newJson = objectMapper.writeValueAsString(dto);
            agentConfig.update(configEtag, newJson);
            repository.save(agentConfig);

        } catch (Exception e) {
            throw new RuntimeException("Pipeline update 실패", e);
        }
    }
}