package com.rental.camprent.dto.request;

import com.rental.camprent.domain.campingitem.CampingItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상태 변경용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @Schema(description = "변경할 장비 상태", example = "UNDER_REPAIR")
    @NotNull
    private CampingItemStatus status;

}
