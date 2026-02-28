package com.rental.camprent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RentalCompleteRequest {

    @Schema(description = "실제 반납일", example = "2026-07-05")
    @NotNull
    private LocalDate returnDate;
}
