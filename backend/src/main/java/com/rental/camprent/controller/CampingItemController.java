package com.rental.camprent.controller;

import com.rental.camprent.dto.request.CampingItemCreateRequest;
import com.rental.camprent.dto.request.CampingItemUpdateRequest;
import com.rental.camprent.dto.request.StatusUpdateRequest;
import com.rental.camprent.dto.request.StockUpdateRequest;
import com.rental.camprent.dto.response.CampingItemResponse;
import com.rental.camprent.service.CampingItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/camping-items")
@RequiredArgsConstructor
public class CampingItemController {

    private final CampingItemService campingItemService;

    /**
     * 캠핑 장비 생성
     */
    @PostMapping
    public ResponseEntity<CampingItemResponse> create(@Valid @RequestBody CampingItemCreateRequest request) {
        CampingItemResponse response = campingItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 캠핑 장비 전체 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CampingItemResponse>> findAll() {
        List<CampingItemResponse> responses = campingItemService.findAll();
        return ResponseEntity.ok(responses);
    }

    /**
     * 캠핑 장비 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<CampingItemResponse> findById(@PathVariable Long id) {
        CampingItemResponse response = campingItemService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 캠핑 장비 정보 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<CampingItemResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CampingItemUpdateRequest request) {
        CampingItemResponse response = campingItemService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 캠핑 장비 재고 증가
     */
    @PutMapping("/{id}/stock/increase")
    public ResponseEntity<CampingItemResponse> increaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        CampingItemResponse response = campingItemService.increaseStock(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 캠핑 장비 재고 감소
     */
    @PutMapping("/{id}/stock/decrease")
    public ResponseEntity<CampingItemResponse> decreaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        CampingItemResponse response = campingItemService.decreaseStock(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 캠핑 장비 상태 변경
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<CampingItemResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        CampingItemResponse response = campingItemService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
