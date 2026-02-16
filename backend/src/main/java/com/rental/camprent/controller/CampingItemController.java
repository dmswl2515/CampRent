package com.rental.camprent.controller;

import com.rental.camprent.dto.request.CampingItemCreateRequest;
import com.rental.camprent.dto.request.CampingItemUpdateRequest;
import com.rental.camprent.dto.request.StatusUpdateRequest;
import com.rental.camprent.dto.request.StockUpdateRequest;
import com.rental.camprent.dto.response.CampingItemResponse;
import com.rental.camprent.service.CampingItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "캠핑 장비", description = "캠핑 장비 관리 API")
@RestController
@RequestMapping("/api/camping-items")
@RequiredArgsConstructor
public class CampingItemController {

    private final CampingItemService campingItemService;

    @Operation(
        summary = "캠핑 장비 등록",
        description = "새로운 캠핑 장비를 등록합니다. 재고 수량과 일일 대여료를 함께 등록합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "장비 등록 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패")
    })
    @PostMapping
    public ResponseEntity<CampingItemResponse> create(@Valid @RequestBody CampingItemCreateRequest request) {
        CampingItemResponse response = campingItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "캠핑 장비 전체 조회",
        description = "등록된 모든 캠핑 장비 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<CampingItemResponse>> findAll() {
        List<CampingItemResponse> responses = campingItemService.findAll();
        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "캠핑 장비 상세 조회",
        description = "ID로 특정 캠핑 장비의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "장비를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CampingItemResponse> findById(
        @Parameter(description = "캠핑 장비 ID", example = "1") @PathVariable Long id
    ) {
        CampingItemResponse response = campingItemService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "캠핑 장비 정보 수정",
        description = "캠핑 장비의 이름, 모델명, 설명, 가격 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @ApiResponse(responseCode = "404", description = "장비를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CampingItemResponse> update(
            @Parameter(description = "캠핑 장비 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody CampingItemUpdateRequest request) {
        CampingItemResponse response = campingItemService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "캠핑 장비 재고 증가",
        description = "캠핑 장비의 재고 수량을 증가시킵니다. (입고 처리)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재고 증가 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @ApiResponse(responseCode = "404", description = "장비를 찾을 수 없음")
    })
    @PutMapping("/{id}/stock/increase")
    public ResponseEntity<CampingItemResponse> increaseStock(
            @Parameter(description = "캠핑 장비 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        CampingItemResponse response = campingItemService.increaseStock(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "캠핑 장비 재고 감소",
        description = "캠핑 장비의 재고 수량을 감소시킵니다. (출고 처리)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재고 감소 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 재고 부족"),
        @ApiResponse(responseCode = "404", description = "장비를 찾을 수 없음")
    })
    @PutMapping("/{id}/stock/decrease")
    public ResponseEntity<CampingItemResponse> decreaseStock(
            @Parameter(description = "캠핑 장비 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        CampingItemResponse response = campingItemService.decreaseStock(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "캠핑 장비 상태 변경",
        description = "캠핑 장비의 상태를 변경합니다. (대여 가능, 수리 중, 대여 불가 등)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @ApiResponse(responseCode = "404", description = "장비를 찾을 수 없음")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<CampingItemResponse> updateStatus(
            @Parameter(description = "캠핑 장비 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        CampingItemResponse response = campingItemService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
