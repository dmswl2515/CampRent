package com.rental.camprent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 응답 DTO (JWT 토큰 포함)
 * */
@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private UserResponse user;

    public static LoginResponse of(String token, UserResponse user) {
        return new LoginResponse(token, user);
    }
}
