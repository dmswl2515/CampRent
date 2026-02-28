package com.rental.camprent.controller;

import com.rental.camprent.domain.campingrental.RentalStatus;
import com.rental.camprent.dto.request.RentalCompleteRequest;
import com.rental.camprent.dto.request.RentalCreateRequest;
import com.rental.camprent.dto.response.RentalResponse;
import com.rental.camprent.service.RentalService;
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

@Tag(name = "대여", description = "대여 관리 API")
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    /**
     * 대여 신청
     * */
    @Operation(summary = "대여 신청", description = "새로운 대여를 신청합니다. 상태는 자동으로 PENDING으로 설정되며, 총 비용이 자동 계산됩니다." )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "대여 신청 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 고객 또는 캠핑장비"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패")
    })
    @PostMapping("/")
    public ResponseEntity<RentalResponse>  create(@Valid @RequestBody RentalCreateRequest request) {
        RentalResponse response = rentalService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 대여 목록 조회
     * */
    @Operation(summary = "대여 목록 조회", description = "전체 대여 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/")
    public ResponseEntity<List<RentalResponse>> findAll() {
        List<RentalResponse> responses = rentalService.findAll();
        return ResponseEntity.ok(responses);
    }

    /**
     * 대여 상세 조회
     * */
    @Operation(summary = "대여 상세 조회", description = "ID로 대여 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대여")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> findById(@Parameter(description = "대여 ID")
                                                   @PathVariable Long id) {
        RentalResponse response = rentalService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 고객별 대여 이력 조회
     * */
    @Operation(summary = "고객별 대여 이력 조회", description = "특정 고객의 모든 대여 이력을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RentalResponse>> findByCustomerId(@Parameter(description = "고객 ID")
                                                                 @PathVariable Long customerId) {
        List<RentalResponse> responses = rentalService.findByCustomerId(customerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 장비별 대여 이력 조회
     * */
    @Operation(summary = "장비별 대여 이력 조회", description = "특정 캠핑 장비의 모든 대여 이력을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "조회 성공")
    })
    @GetMapping("/camping-item/{machineId}")
    public ResponseEntity<List<RentalResponse>> findByMachineId(@Parameter(description = "캠핑 장비 ID")
                                                                @PathVariable Long machineId) {
        List<RentalResponse> responses = rentalService.findByMachineId(machineId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 상태별 대여 조회
     * */
    @Operation(summary = "상태별 대여 조회", description = "특정 상태의 대여 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RentalResponse>> findByStatus(@Parameter(description = "대여 상태 (PENDING, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED, OVERDUE 등)")
                                                             @PathVariable RentalStatus status) {
        List<RentalResponse> responses = rentalService.findByStatus(status);
        return ResponseEntity.ok(responses);
    }

    // ===== 상태 전환 엔드포인트 =====
    /**
     * 대여 승인
     * */
    @Operation(summary = "대여 승인", description =
            "대여 신청을 승인합니다. PENDING → APPROVED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대여"),
            @ApiResponse(responseCode = "400", description = "승인 불가능한 상태")
    })
    @PostMapping("/{id}/approve")
    public ResponseEntity<RentalResponse> approve(@Parameter(description = "대여 ID")
                                                  @PathVariable Long id) {
        RentalResponse response = rentalService.approve(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 대여 거부
     * */
    @Operation(summary = "대여 거부", description = "대여 신청을 거부합니다. PENDING → CANCELLED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거부 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대여")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<RentalResponse> reject(@Parameter(description = "대여 ID")
                                                 @PathVariable Long id) {
        RentalResponse response = rentalService.reject(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 대여 시작
     * */
    @Operation(summary = "대여 시작", description = "대여를 시작합니다. APPROVED → IN_PROGRESS (재고 자동 감소)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대여 시작 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대여"),
            @ApiResponse(responseCode = "400", description = "대여 시작 불가능한 상태 또는 재고 부족")
    })
    @PostMapping("/{id}/start")
    public ResponseEntity<RentalResponse> start(@Parameter(description = "대여 ID")
                                                @PathVariable Long id) {
        RentalResponse response = rentalService.start(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 반납 처리
     * POST /api/rentals/{id}/complete
     *
     * @RequestBody RentalCompleteRequest: 실제 반납일을 받음
     * 왜 필요? endDate(계약일)과 actualReturnDate(실제 반납일)이 다를 수 있어서
     * 예: 계약 7월 5일까지인데 실제 7월 7일 반납 → 2일 연체
     */
    @Operation(summary = "반납 처리", description = "대여를 반납 처리합니다. IN_PROGRESS/OVERDUE → COMPLETED (재고 자동 복구)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반납 처리 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대여"),
            @ApiResponse(responseCode = "400", description = "반납 불가능한 상태")
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<RentalResponse> complete(
            @Parameter(description = "대여 ID") @PathVariable Long id,
            @Valid @RequestBody RentalCompleteRequest request
    ) {
        RentalResponse response = rentalService.complete(id, request.getReturnDate());
        return ResponseEntity.ok(response);
    }

    /**
     * 대여 취소
     * POST /api/rentals/{id}/cancel
     *
     * 주의: 대여중(IN_PROGRESS)이었다면 재고 복구됨
     * PENDING 상태 취소는 재고 영향 없음
     */
    @Operation(summary = "대여 취소", description = "대여를 취소합니다. 대여중이었다면 재고를 복구합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대여"),
            @ApiResponse(responseCode = "400", description = "취소 불가능한 상태 (이미 완료됨)")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<RentalResponse> cancel(
            @Parameter(description = "대여 ID") @PathVariable Long id
    ) {
        RentalResponse response = rentalService.cancel(id);
        return ResponseEntity.ok(response);
    }
}
