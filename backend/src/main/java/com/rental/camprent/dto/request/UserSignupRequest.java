package com.rental.camprent.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignupRequest (

    @NotBlank(message = "아이디는 필수 입니다.")
    @Size(min = 4, max = 20, message = "아이디는 3~20자 사이어야 합니다.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 영문 소문자와 숫자만 가능합니다.")
    String username,

    @NotBlank(message = "비밀번호는 필수 입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
            message = "비밀번호는 영문, 숫자,특수문자를 포함해야 합니다.")
    String password,

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 10, message = "이름은 2~10자 사이여야 합니다.")
    String name,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$",
             message = "전화번호 형식이 올바르지 않습니다. (예:010-1234-5678)")
    String phone
) {
    // UserRole은 회원가입 시 항상 USER로 고정
    // 관리자는 직접 DB에서 수정하거나 별도 API로 생성
}
