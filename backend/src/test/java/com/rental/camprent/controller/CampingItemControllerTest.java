package com.rental.camprent.controller;

import com.rental.camprent.config.JpaAuditingConfiguration;
import com.rental.camprent.domain.campingitem.CampingCategory;
import com.rental.camprent.domain.campingitem.CampingItemStatus;
import com.rental.camprent.dto.request.CampingItemCreateRequest;
import com.rental.camprent.dto.request.CampingItemUpdateRequest;
import com.rental.camprent.dto.request.StatusUpdateRequest;
import com.rental.camprent.dto.request.StockUpdateRequest;
import com.rental.camprent.dto.response.CampingItemResponse;
import com.rental.camprent.service.CampingItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = CampingItemController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaAuditingConfiguration.class)
)
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 비활성화
class CampingItemControllerTest {

    @Autowired
    private MockMvc mockMvc;    // HTTP 요청/응답 시뮬레이션

    @Autowired
    private ObjectMapper objectMapper;  // Java 객체 <-> JSON 변환

    @MockitoBean
    private CampingItemService campingItemService; // Service를 Mock 객체로 대체

    @Test
    @DisplayName("캠핑 장비 생성 성공")
    void create_Success() throws Exception {
        // Given: 테스트 데이터 준비
        CampingItemCreateRequest request = new CampingItemCreateRequest(
                "4인용 텐트",
                CampingCategory.TENT,
                "MSR",
                "4인 가족이 사용하기 좋은 텐트",
                10,
                new BigDecimal("50000"),
                CampingItemStatus.AVAILABLE
        );

        CampingItemResponse response = new CampingItemResponse(
                1L,
                "4인용 텐트",
                CampingCategory.TENT,
                "MSR",
                "4인 가족이 사용하기 좋은 텐트",
                10,
                new BigDecimal("50000"),
                CampingItemStatus.AVAILABLE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Service의 create() 메서드가 호출되면 response를 반환하도록 설정
        given(campingItemService.create(any(CampingItemCreateRequest.class)))
                .willReturn(response);

        // When & Then: API 호출 및 검증
        mockMvc.perform(post("/api/camping-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())    // 201
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("4인용 텐트"))
                .andExpect(jsonPath("$.category").value("TENT"))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.baseDailyRate").value(50000));
    }

    @Test
    @DisplayName("캠핑 장비 전체 조회 성공")
    void findAll_Success() throws Exception {
        // Given
        CampingItemResponse response1 = new CampingItemResponse(
                1L, "4인용 텐트", CampingCategory.TENT, "MSR",
                "4인 가족이 사용하기 좋은 텐트", 10, new BigDecimal("50000"),
                CampingItemStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now()
        );

        CampingItemResponse response2 = new CampingItemResponse(
                2L, "침낭", CampingCategory.SLEEPING_BAG, "Model-B",
                "겨울용 침낭", 5, new BigDecimal("30000"),
                CampingItemStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now()
        );

        given(campingItemService.findAll())
                .willReturn(List.of(response1, response2));

        // When & Then
        mockMvc.perform(get("/api/camping-items"))
                .andExpect(status().isOk())  // 200
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("4인용 텐트"))
                .andExpect(jsonPath("$[1].name").value("침낭"));
    }

    @Test
    @DisplayName("캠핑 장비 상세 조회 성공")
    void findById_Success() throws Exception {
        // Given
        Long itemId = 1L;
        CampingItemResponse response = new CampingItemResponse(
                itemId, "4인용 텐트", CampingCategory.TENT, "MSR",
                "4인 가족이 사용하기 좋은 텐트", 10, new BigDecimal("50000"),
                CampingItemStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now()
        );

        given(campingItemService.findById(itemId))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/camping-items/{id}", itemId))
                .andExpect(status().isOk())     // 200
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("4인용 텐트"));
    }

    @Test
    @DisplayName("캠핑 장비 정보 수정 성공")
    void update_Success() throws Exception {
        // Given
        Long itemId = 1L;
        CampingItemUpdateRequest request = new CampingItemUpdateRequest(
                "5인용 텐트",
                "MSR-Pro",
                "업그레이드된 가족용 텐트",
                new BigDecimal("60000")
        );

        CampingItemResponse response = new CampingItemResponse(
                itemId, "5인용 텐트", CampingCategory.TENT, "MSR-Pro",
                "업그레이드된 가족용 텐트", 10, new BigDecimal("60000"),
                CampingItemStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now()
        );

        given(campingItemService.update(eq(itemId), any(CampingItemUpdateRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/camping-items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("5인용 텐트"))
                .andExpect(jsonPath("$.model").value("MSR-Pro"))
                .andExpect(jsonPath("$.baseDailyRate").value(60000));
    }

    @Test
    @DisplayName("캠핑 장비 재고 증가 성공")
    void increaseStock_Success() throws Exception {
        // Given
        Long itemId = 1L;
        StockUpdateRequest request = new StockUpdateRequest(5);

        CampingItemResponse response = new CampingItemResponse(
                itemId, "4인용 텐트", CampingCategory.TENT, "MSR",
                "4인 가족이 사용하기 좋은 텐트", 15, new BigDecimal("50000"),
                CampingItemStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now()
        );

        given(campingItemService.increaseStock(eq(itemId), any(StockUpdateRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/camping-items/{id}/stock/increase", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(15));
    }

    @Test
    @DisplayName("캠핑 장비 재고 감소 성공")
    void decreaseStock_Success() throws Exception {
        // Given
        Long itemId = 1L;
        StockUpdateRequest request = new StockUpdateRequest(3);

        CampingItemResponse response = new CampingItemResponse(
                itemId, "4인용 텐트", CampingCategory.TENT, "MSR",
                "4인 가족이 사용하기 좋은 텐트", 7, new BigDecimal("50000"),
                CampingItemStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now()
        );

        given(campingItemService.decreaseStock(eq(itemId), any(StockUpdateRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/camping-items/{id}/stock/decrease", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(7));
    }

    @Test
    @DisplayName("캠핑 장비 상태 변경 성공")
    void updateStatus_Success() throws Exception {
        // Given
        Long itemId = 1L;
        StatusUpdateRequest request = new StatusUpdateRequest(CampingItemStatus.UNDER_REPAIR);

        CampingItemResponse response = new CampingItemResponse(
                itemId, "4인용 텐트", CampingCategory.TENT, "MSR",
                "4인 가족이 사용하기 좋은 텐트", 10, new BigDecimal("50000"),
                CampingItemStatus.UNDER_REPAIR, LocalDateTime.now(), LocalDateTime.now()
        );

        given(campingItemService.updateStatus(eq(itemId), any(StatusUpdateRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/camping-items/{id}/status", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REPAIR"));
    }

    @Test
    @DisplayName("이름이 공백이면 생성 실패")
    void create_ValidationFail_WhenNameIsBlank() throws Exception {
        // Given: name이 공백인 요청
        CampingItemCreateRequest request = new CampingItemCreateRequest(
                "    ",     // 공백
                CampingCategory.TENT,
                "MSR",
                "4인 가족이 사용하기 좋은 텐트",
                10,
                new BigDecimal("50000"),
                CampingItemStatus.AVAILABLE
        );

        // When & Then
        mockMvc.perform(post("/api/camping-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());   // 400
    }
}
