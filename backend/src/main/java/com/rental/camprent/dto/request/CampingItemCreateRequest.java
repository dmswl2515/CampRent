package com.rental.camprent.dto.request;

import com.rental.camprent.domain.campingitem.CampingCategory;
import com.rental.camprent.domain.campingitem.CampingItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CampingItemCreateRequest {

    @Schema(description = "캠핑 장비 이름", example = "4인용 텐트")
    @NotBlank
    private String name;

    @Schema(description = "카테고리", example = "TENT")
    @NotNull
    private CampingCategory category;

    @Schema(description = "모델명", example = "MSR-2024")
    @NotBlank
    private String model;

    @Schema(description = "장비 설명", example = "가족 단위로 사용하기 좋은 4인용 텐트입니다.")
    @NotBlank
    private String description;

    @Schema(description = "재고 수량", example = "10")
    @NotNull
    private Integer stockQuantity;

    @Schema(description = "1일 대여료", example = "50000")
    @NotNull
    private BigDecimal baseDailyRate;

    @Schema(description = "장비 상태", example = "AVAILABLE")
    @NotNull
    private CampingItemStatus status;
}
