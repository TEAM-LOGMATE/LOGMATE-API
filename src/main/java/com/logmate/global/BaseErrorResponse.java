package com.logmate.global;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BaseErrorResponse {
    private int status;         // HTTP 상태 코드
    private String error;       // 에러 타입 (Bad Request, Not Found 등)
    private String message;     // 상세 메시지
    private String path;        // 요청 경로
    private LocalDateTime timestamp; // 발생 시각

    public static BaseErrorResponse of(int status, String error, String message, String path) {
        return new BaseErrorResponse(status, error, message, path, LocalDateTime.now());
    }
}