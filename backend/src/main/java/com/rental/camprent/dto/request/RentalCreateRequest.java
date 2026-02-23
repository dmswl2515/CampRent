package com.rental.camprent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RentalCreateRequest {

    @Schema(description = "고객 ID", example = "1")
    @NotNull
    private Long customerId;

    @Schema(description = "캠핑 장비 ID", example = "1")
    @NotNull
    private Long campingItemId;

    @Schema(description = "대여 시작일", example = "2026-01-01")
    @NotNull
    private LocalDate startDate;

    @Schema(description = "대여 종료일", example = "2026-01-08")
    @NotNull
    private LocalDate endDate;

    @Schema(description = "보증금", example = "50000")
    @NotNull
    private BigDecimal deposit;

    @Schema(description = "비고 (특이사항)", example = "텐트 파손 주의")
    private String notes;
}
