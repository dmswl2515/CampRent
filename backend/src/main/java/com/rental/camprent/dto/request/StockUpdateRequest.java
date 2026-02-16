package com.rental.camprent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 증감용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {

    @Schema(description = "증감할 재고 수량", example = "5")
    @NotNull
    private Integer quantity;

}
