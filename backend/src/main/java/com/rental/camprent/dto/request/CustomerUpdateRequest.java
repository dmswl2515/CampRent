package com.rental.camprent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {

    @Schema(description = "고객명", example = "홍길동")
    @NotBlank
    private String name;

    @Schema(description = "연락처 (010-XXXX-XXXX)", example = "010-9876-5432")
    @NotBlank
    private String phone;

    @Schema(description = "이메일", example = "camp2@example.com")
    @Email
    private String email;

    @Schema(description = "주소", example = "서울시 서초구 서초대로 456")
    private String address;
}
