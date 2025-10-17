package com.logmate.webhook.controller;

import com.logmate.global.BaseResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final UserRepository userRepository;

    //등록
    @PostMapping
    public ResponseEntity<BaseResponse<WebhookResponseDto>> register(
            @RequestBody WebhookRequestDto dto,
            HttpServletRequest request
    ) {
        String email = (String) request.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."));
        WebhookResponseDto saved = webhookService.register(user.getId(), dto);
        return ResponseEntity.ok(BaseResponse.of(200, "Webhook 등록 성공", saved));
    }

    //테스트
    @PostMapping("/test")
    public ResponseEntity<BaseResponse<Void>> testWebhook(@RequestParam String url) {
        webhookService.testSend(url);
        return ResponseEntity.ok(BaseResponse.of(200, "Webhook 테스트 전송 성공",null));
    }

    //등록된 Webhook으로 알림 전송
    @PostMapping("/trigger")
    public ResponseEntity<BaseResponse<Void>> trigger(
            @RequestParam String message,
            HttpServletRequest request
    ) {
        String email = (String) request.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        webhookService.sendEventToUserWebhooks(user.getId(), message);
        return ResponseEntity.ok(BaseResponse.of(200, "Webhook 알림 전송 성공",null));
    }

    //본인의 웹훅 url 조회
    @GetMapping
    public ResponseEntity<BaseResponse<List<WebhookResponseDto>>> getUserWebhooks(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<WebhookResponseDto> webhooks = webhookService.getUserWebhooks(user.getId());
        return ResponseEntity.ok(BaseResponse.of(200, "Webhook 목록 조회 성공",webhooks));
    }

    //Webhook 삭제
    @DeleteMapping("/{webhookId}")
    public ResponseEntity<BaseResponse<Void>> deleteWebhook(
            @PathVariable Long webhookId,
            HttpServletRequest request
    ) {
        String email = (String) request.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        webhookService.deleteWebhook(user.getId(), webhookId);
        return ResponseEntity.ok(BaseResponse.of(200, "Webhook 삭제 성공", null));
    }
}