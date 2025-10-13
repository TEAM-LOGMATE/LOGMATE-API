package com.logmate.webhook.service;

import com.logmate.global.CustomException;
import com.logmate.webhook.dto.WebhookRequestDto;
import com.logmate.webhook.dto.WebhookResponseDto;
import com.logmate.webhook.model.Webhook;
import com.logmate.webhook.model.WebhookType;
import com.logmate.webhook.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookRepository webhookRepository;

    public WebhookResponseDto register(Long userId, WebhookRequestDto dto) {
        if (!isValidUrl(dto.getUrl())) {
            throw new IllegalArgumentException("유효하지 않은 URL입니다.");
        }

        boolean existUrl = webhookRepository.existsByUserIdAndUrl(userId,dto.getUrl());
        if(existUrl){
            throw new CustomException(HttpStatus.CONFLICT, "이미 등록된 URL입니다.");
        }

        Webhook webhook = new Webhook();
        webhook.setUserId(userId);
        webhook.setType(dto.getType());
        webhook.setUrl(dto.getUrl());

        Webhook saved = webhookRepository.save(webhook);
        return new WebhookResponseDto(
                saved.getId(),
                saved.getType(),
                saved.getUrl(),
                saved.isActive()
        );
    }

    private boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme().startsWith("http");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public void testSend(String url) {
        Map<String, String> payload;

        if (url.contains("discord.com")) {
            payload = Map.of("content", "Webhook 테스트 메시지입니다."); //Discord 전용
        } else {
            payload = Map.of("text", "Webhook 테스트 메시지입니다.");    //Slack/Custom
        }

        WebClient.create()
                .post()
                .uri(url)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    //웹훅 알림 전송 메서드
    public void sendEventToUserWebhooks(Long userId, String message) {
        List<Webhook> webhooks = webhookRepository.findByUserIdAndActiveTrue(userId);

        for (Webhook webhook : webhooks) {
            try {
                Map<String, String> payload;

                // 타입별 payload 분기
                if (webhook.getType() == WebhookType.DISCORD) {
                    payload = Map.of("content", message); //Discord는 content 필드
                } else {
                    payload = Map.of("text", message);    //Slack/Custom은 text 필드
                }
                WebClient.create()
                        .post()
                        .uri(webhook.getUrl())
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Webhook 전송 성공: {}", webhook.getUrl());

            } catch (Exception e) {
                log.warn("Webhook 전송 실패: {}", webhook.getUrl(), e);
            }
        }
    }

    //웹훅 삭제 메서드
    public void deleteWebhook(Long userId, Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 url을 찾을 수 없습니다."));

        if(!webhook.getUserId().equals(userId)){
            throw new CustomException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }
        webhookRepository.delete(webhook);
    }

    //웹훅 조회 메서드
    public List<WebhookResponseDto> getUserWebhooks(Long userId) {
        List<Webhook> webhooks = webhookRepository.findByUserId(userId);

        if (webhooks.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "등록된 Webhook이 없습니다.");
        }

        return webhooks.stream()
                .map(w -> new WebhookResponseDto(
                        w.getId(),
                        w.getType(),
                        w.getUrl(),
                        w.isActive()
                ))
                .toList();
    }
}
