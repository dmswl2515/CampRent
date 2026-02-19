package com.rental.camprent.dto.response;

import com.rental.camprent.domain.customer.Customer;
import com.rental.camprent.domain.customer.CustomerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerResponse {
    @Schema(description = "고객 ID", example = "1")
    private Long id;

    @Schema(description = "고객명", example = "홍길동")
    private String name;

    @Schema(description = "연락처", example = "010-1234-5678")
    private String phone;

    @Schema(description = "이메일", example = "camp@example.com")
    private String email;

    @Schema(description = "고객 유형", example = "INDIVIDUAL")
    private CustomerType type;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "사업자등록번호", example = "123-45-67890")
    private String businessNumber;

    @Schema(description = "등록일시", example = "2026-01-01T10:00:00")
    private LocalDateTime createAt;

    @Schema(description = "수정일시", example = "2026-01-02T12:00:00")
    private LocalDateTime updatedAt;

    public static CustomerResponse from(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .type(customer.getType())
                .address(customer.getAddress())
                .businessNumber(customer.getBusinessNumber())
                .createAt(customer.getCreatedAt())
                .updatedAt(customer.getCreatedAt())
                .build();
    }
}
