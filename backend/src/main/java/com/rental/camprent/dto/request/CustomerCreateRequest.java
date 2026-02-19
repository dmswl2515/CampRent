package com.rental.camprent.dto.request;

import com.rental.camprent.domain.customer.CustomerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @Schema(description = "고객명", example = "홍길동")
    @NotBlank
    private String name;

    @Schema(description = "연락처 (010-XXXX-XXXX)", example = "010-1234-5678")
    @NotBlank
    private String phone;

    @Schema(description = "이메일", example = "camp@example.com")
    @Email
    private String email;

    @Schema(description = "고객 유형 (INDIVIDUAL: 개인, BUSINESS: 기업)", example = "INDIVIDUAL")
    @NotNull
    private CustomerType type;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "사업자등록번호 (기업 고객인 경우 필수)", example = "123-45-67890")
    private String businessNumber;
}

// 근데 나 약간 당근 벤치마킹하고 싶거든? 동네에서 빌리는 걸로? 근데 고객유형이 좀 걸리넹,,, 먼가 동네에서 빌리는건데 필요할까 싶어서

