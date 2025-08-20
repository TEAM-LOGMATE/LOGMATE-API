package com.logmate.global;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BaseResponse<T> {
    private int status;        // HTTP 상태 코드
    private String message;    // 성공 메시지
    private T data;            // 실제 응답 데이터
    private LocalDateTime timestamp;

    public static <T> BaseResponse<T> of(int status, String message, T data) {
        return new BaseResponse<>(status, message, data, LocalDateTime.now());
    }
}
