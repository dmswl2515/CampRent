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
public class CampingItemUpdateRequest {

    @Schema(description = "캠핑 장비 이름", example = "5인용 텐트")
    @NotBlank
    private String name;

    @Schema(description = "모델명", example = "MSR-Pro-2024")
    @NotBlank
    private String model;

    @Schema(description = "장비 설명", example = "업그레이드된 가족용 텐트입니다.")
    @NotBlank
    private String description;

    @Schema(description = "1일 대여료", example = "60000")
    @NotNull
    private BigDecimal baseDailyRate;

}
