package com.logmate.webhook.controller;

import com.logmate.global.CustomException;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import com.logmate.webhook.dto.WebhookRequestDto;
import com.logmate.webhook.dto.WebhookResponseDto;
import com.logmate.webhook.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final UserRepository userRepository;

    //등록
    @PostMapping
    public ResponseEntity<WebhookResponseDto> register(
            @RequestBody WebhookRequestDto dto,
            HttpServletRequest request
    ) {
        String email = (String) request.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("인증되지 않은 사용자입니다"));
        return ResponseEntity.ok(webhookService.register(user.getId(), dto));
    }

    //테스트
    @PostMapping("/test")
    public ResponseEntity<Void> testWebhook(@RequestParam String url) {
        webhookService.testSend(url);
        return ResponseEntity.ok().build();
    }

    //등록된 Webhook으로 알림 전송
    @PostMapping("/trigger")
    public ResponseEntity<Void> trigger(
            @RequestParam String message,
            HttpServletRequest request
    ) {
        String email = (String) request.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        webhookService.sendEventToUserWebhooks(user.getId(), message);
        return ResponseEntity.ok().build();
    }
}