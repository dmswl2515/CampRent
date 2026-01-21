package com.rental.camprent.service;

import com.rental.camprent.domain.campingitem.CampingCategory;
import com.rental.camprent.domain.campingitem.CampingItem;
import com.rental.camprent.domain.campingitem.CampingItemRepository;
import com.rental.camprent.domain.campingitem.CampingItemStatus;
import com.rental.camprent.dto.request.CampingItemCreateRequest;
import com.rental.camprent.dto.request.CampingItemUpdateRequest;
import com.rental.camprent.dto.request.StatusUpdateRequest;
import com.rental.camprent.dto.request.StockUpdateRequest;
import com.rental.camprent.dto.response.CampingItemResponse;
import com.rental.camprent.exception.ItemNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CampingItemServiceTest {

    @Mock
    private CampingItemRepository campingItemRepository;

    @InjectMocks
    private CampingItemService campingItemService;

    @Test
    @DisplayName("ID로 캠핑 장비 조회 성공")
    void findById_Success() {
        // Given: 테스트 데이터 준비
        Long itemId = 1L;
        CampingItem entity = CampingItem.builder()
                .name("4인용 텐트")
                .category(CampingCategory.TENT)
                .model("MSR")
                .description("가족용 텐트")
                .stockQuantity(10)
                .baseDailyRate(new BigDecimal("50000"))
                .status(CampingItemStatus.AVAILABLE)
                .build();

        // Repository의 findById()가 호출되면 위에서 만든 entity를 반환하도록 설정
        given(campingItemRepository.findById(itemId))
                .willReturn(Optional.of(entity));

        // When: 실제 메서드 호출
        CampingItemResponse response = campingItemService.findById(itemId);

        // Then: 결과 검증
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("4인용 텐트");
        assertThat(response.getCategory()).isEqualTo(CampingCategory.TENT);
        assertThat(response.getStockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("ID로 캠핑 장비 조회 실패 - 존재하지 않는 ID")
    void findById_NotFound() {
        // Given: 존재하지 않는 ID
        Long nonExistentId = 999L;

        // Repository의 findById()가 빈 Optional을 반환하도록 설정
        given(campingItemRepository.findById(nonExistentId))
                .willReturn(Optional.empty());

        // When & Then: 예외 발생 검증
        assertThrows(ItemNotFoundException.class, () -> {
            campingItemService.findById(nonExistentId);
        });
    }

    @Test
    @DisplayName("캠핑 장비 생성 성공")
    void create_Success() {
        // Given: 생성 요청 데이터
        CampingItemCreateRequest request = new CampingItemCreateRequest(
                "4인용 텐트",
                CampingCategory.TENT,
                "MSR",
                "가족용 텐트",
                10,
                new BigDecimal("50000"),
                CampingItemStatus.AVAILABLE
        );

        // Repository.save()가 호출되면 저장된 Entity를 반환하도록 설정
        CampingItem savedEntity = CampingItem.builder()
                .name(request.getName())
                .category(request.getCategory())
                .model(request.getModel())
                .description(request.getDescription())
                .stockQuantity(request.getStockQuantity())
                .baseDailyRate(request.getBaseDailyRate())
                .status(request.getStatus())
                .build();

        given(campingItemRepository.save(any(CampingItem.class)))
                .willReturn(savedEntity);

        // When: create 메서드 호출
        CampingItemResponse response = campingItemService.create(request);

        // Then: 결과 검증
        assertThat(response.getName()).isEqualTo("4인용 텐트");
        assertThat(response.getCategory()).isEqualTo(CampingCategory.TENT);
        assertThat(response.getStockQuantity()).isEqualTo(10);

        // Repository의 save() 메서드가 1번 호출되었는지 검증
        verify(campingItemRepository, times(1)).save(any(CampingItem.class));
    }

    @Test
    @DisplayName("캠핑 장비 정보 수정 성공")
    void update_Success() {
        // Given: 기존 Entity
        Long itemId = 1L;
        CampingItem existingEntity = CampingItem.builder()
                .name("4인용 텐트")
                .category(CampingCategory.TENT)
                .model("MSR")
                .description("가족용 텐트")
                .stockQuantity(10)
                .baseDailyRate(new BigDecimal("50000"))
                .status(CampingItemStatus.AVAILABLE)
                .build();

        // 수정 요청 데이터
        CampingItemUpdateRequest updateRequest = new CampingItemUpdateRequest(
                "5인용 텐트",  // 이름 변경
                "MSR-Pro",     // 모델 변경
                "업그레이드된 가족용 텐트",
                new BigDecimal("60000")
        );

        given(campingItemRepository.findById(itemId))
                .willReturn(Optional.of(existingEntity));

        // When: update 메서드 호출
        CampingItemResponse response = campingItemService.update(itemId, updateRequest);

        // Then: 결과 검증
        assertThat(response.getName()).isEqualTo("5인용 텐트");
        assertThat(response.getModel()).isEqualTo("MSR-Pro");
        assertThat(response.getBaseDailyRate()).isEqualTo(new BigDecimal("60000"));
    }

    @Test
    @DisplayName("재고 증가 성공")
    void increaseStock_Success() {
        // Given: 기존 재고가 10인 Entity
        Long itemId = 1L;
        CampingItem entity = CampingItem.builder()
                .name("4인용 텐트")
                .category(CampingCategory.TENT)
                .model("MSR")
                .description("가족용 텐트")
                .stockQuantity(10)  // 기존 재고 10
                .baseDailyRate(new BigDecimal("50000"))
                .status(CampingItemStatus.AVAILABLE)
                .build();

        StockUpdateRequest request = new StockUpdateRequest(5);  // 5개 증가

        given(campingItemRepository.findById(itemId))
                .willReturn(Optional.of(entity));

        // When: 재고 증가
        CampingItemResponse response = campingItemService.increaseStock(itemId, request);

        // Then: 재고가 15개가 되었는지 검증
        assertThat(response.getStockQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("재고 감소 성공")
    void decreaseStock_Success() {
        // Given: 기존 재고가 10인 Entity
        Long itemId = 1L;
        CampingItem entity = CampingItem.builder()
                .name("4인용 텐트")
                .category(CampingCategory.TENT)
                .model("MSR")
                .description("가족용 텐트")
                .stockQuantity(10)  // 기존 재고 10
                .baseDailyRate(new BigDecimal("50000"))
                .status(CampingItemStatus.AVAILABLE)
                .build();

        StockUpdateRequest request = new StockUpdateRequest(3);  // 3개 감소

        given(campingItemRepository.findById(itemId))
                .willReturn(Optional.of(entity));

        // When: 재고 감소
        CampingItemResponse response = campingItemService.decreaseStock(itemId, request);

        // Then: 재고가 7개가 되었는지 검증
        assertThat(response.getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("상태 변경 성공")
    void updateStatus_Success() {
        // Given: 상태가 AVAILABLE인 Entity
        Long itemId = 1L;
        CampingItem entity = CampingItem.builder()
                .name("4인용 텐트")
                .category(CampingCategory.TENT)
                .model("MSR")
                .description("가족용 텐트")
                .stockQuantity(10)
                .baseDailyRate(new BigDecimal("50000"))
                .status(CampingItemStatus.AVAILABLE)  // 현재 상태: AVAILABLE
                .build();

        StatusUpdateRequest request = new StatusUpdateRequest(
                CampingItemStatus.UNDER_REPAIR  // UNDER_REPAIR로 변경
        );

        given(campingItemRepository.findById(itemId))
                .willReturn(Optional.of(entity));

        // When: 상태 변경
        CampingItemResponse response = campingItemService.updateStatus(itemId, request);

        // Then: 상태가 UNDER_REPAIR로 변경되었는지 검증
        assertThat(response.getStatus()).isEqualTo(CampingItemStatus.UNDER_REPAIR);
    }
}
