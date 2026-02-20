package com.rental.camprent.controller;

import com.rental.camprent.domain.customer.CustomerType;
import com.rental.camprent.dto.request.CustomerCreateRequest;
import com.rental.camprent.dto.request.CustomerUpdateRequest;
import com.rental.camprent.dto.response.CustomerResponse;
import com.rental.camprent.service.CustomerService;
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

@Tag(name = "고객", description = "고객 관리 API")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "고객 목록 조회", description = "전체 고객 목록을 조회합니다. type 파라미터로 유형 필터링 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll(
            @Parameter(description = "고객 유형 필터(INDIVIDUAL, BUSINESS)")
            @RequestParam(required = false) CustomerType type
    ) {
        if (type != null) {
            List<CustomerResponse> responses = customerService.findByType(type);
            return ResponseEntity.ok(responses);
        }
        List<CustomerResponse> responses = customerService.findAll();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "고객 상세 조회", description= "ID로 고객 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 고객")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(
            @Parameter(description = "고객 ID") @PathVariable Long id
    ) {
        CustomerResponse response = customerService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "고객 등록",
            description = "새로운 고객을 등록합니다. " +
                          "현재는 개인 고객(INDIVIDUAL) 위주로 운영되며, " +
                          "기업 고객(BUSINESS) 기능은 향후 확장 예정입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패")
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "고객 정보 수정", description = "고객의 기본 정보를 수정합니다. 고객 유형(type)은 변경 불가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 고객")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @Parameter(description = "고객 ID") @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        CustomerResponse response = customerService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "고객 삭제", description = "고객 정보를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 고객")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "고객 ID")
                                       @PathVariable Long id
    ) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
