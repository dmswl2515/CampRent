package com.rental.camprent.dto.response;

import com.rental.camprent.domain.campingrental.Rental;
import com.rental.camprent.domain.campingrental.RentalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class RentalResponse {

    @Schema(description = "대여 ID", example = "1")
    private Long id;

    @Schema(description = "고객 ID", example = "1")
    private Long customerId;

    @Schema(description = "고객명", example = "홍길동")
    private String customerName;

    @Schema(description = "캠핑 장비 ID", example = "1")
    private Long campingItemId;

    @Schema(description = "캠핑 장비명", example = "5인용 텐트")
    private String campingItemName;

    @Schema(description = "대여 시작일", example = "2026-01-01")
    private LocalDate startDate;

    @Schema(description = "대여 종료일", example = "2026-01-08")
    private LocalDate endDate;

    @Schema(description = "실제 반납일", example = "2026-01-08")
    private LocalDate actualReturnDate;

    @Schema(description = "총 대여 비용", example = "25000")
    private BigDecimal totalCost;

    @Schema(description = "보증금", example = "50000")
    private BigDecimal deposit;

    @Schema(description = "대여 상태", example = "PENDING")
    private RentalStatus status;

    @Schema(description = "비고 (특이사항)", example = "텐트 파손 주의")
    private String notes;

    @Schema(description = "신청일시", example = "2026-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2026-01-21T14:30:00")
    private LocalDateTime updatedAt;

    public static RentalResponse from(Rental rental) {
        return RentalResponse.builder()
                .id(rental.getId())
                .customerId(rental.getCustomer().getId())
                .customerName(rental.getCustomer().getName())
                .campingItemId(rental.getMachine().getId())
                .campingItemName(rental.getMachine().getName())
                .startDate(rental.getStartDate())
                .endDate(rental.getEndDate())
                .actualReturnDate(rental.getActualReturnDate())
                .totalCost(rental.getTotalCost())
                .deposit(rental.getTotalCost())
                .status(rental.getStatus())
                .notes(rental.getNotes())
                .createdAt(rental.getCreatedAt())
                .updatedAt(rental.getUpdatedAt())
                .build();
    }


}
