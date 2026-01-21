package com.rental.camprent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private int status;                 // HTTP 상태 코드 (404, 400, 500)
    private String code;                // 에러코드
    private String message;             // 에러 메세지
    private LocalDateTime timestamp;    // 발생 시작

    // 정적 팩토리 메서드
    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, LocalDateTime.now());
    }


}
