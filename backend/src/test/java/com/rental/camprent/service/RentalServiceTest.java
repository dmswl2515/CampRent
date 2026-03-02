package com.rental.camprent.service;

import com.rental.camprent.domain.campingitem.CampingCategory;
import com.rental.camprent.domain.campingitem.CampingItem;
import com.rental.camprent.domain.campingitem.CampingItemRepository;
import com.rental.camprent.domain.campingitem.CampingItemStatus;
import com.rental.camprent.domain.campingrental.Rental;
import com.rental.camprent.domain.campingrental.RentalRepository;
import com.rental.camprent.domain.campingrental.RentalStatus;
import com.rental.camprent.domain.customer.Customer;
import com.rental.camprent.domain.customer.CustomerRepository;
import com.rental.camprent.domain.customer.CustomerType;
import com.rental.camprent.dto.request.RentalCreateRequest;
import com.rental.camprent.dto.response.RentalResponse;
import com.rental.camprent.exception.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * RentalService 단위 테스트
 * */
@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CampingItemRepository campingItemRepository;

    @InjectMocks
    private RentalService rentalService;

    private Customer customer;
    private CampingItem campingItem;
    private Rental rental;

    @BeforeEach
    void setUp() {
       customer = Customer.builder()
               .name("홍길동")
               .phone("010-1234-5678")
               .email("kim@example.com")
               .type(CustomerType.INDIVIDUAL)
               .address("서울시 강남구")
               .build();

        campingItem = CampingItem.builder()
                .name("5인용 텐트")
                .category(CampingCategory.TENT)
                .model("MSR-Pro-2024")
                .description("가족용 텐트")
                .stockQuantity(10)
                .baseDailyRate(BigDecimal.valueOf(50000))
                .status(CampingItemStatus.AVAILABLE)
                .build();

        rental = Rental.builder()
                .customer(customer)
                .machine(campingItem)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 5))
                .deposit(BigDecimal.valueOf(50000))
                .notes("테스트 대여")
                .build();

        // 만약 테스트 데이터 내가 직접 작성해야 한다면, 엔티티 보고 작성하면 되려나?
    }

    // BDD는 무슨 약자야?
    @Test
    @DisplayName("대여 신청 성공")
    void create_Success() {
        // Given : Mock Repository의 동작을 정의
        given(customerRepository.findById(1L))
                .willReturn(Optional.of(customer)); // Optional은 값이 Null일 경우를 대비한거야?

        given(campingItemRepository.findById(2L))
                .willReturn(Optional.of(campingItem));

        // any(Rental.class)
        given(rentalRepository.save(any(Rental.class))) // any 개념이 잘 이해가 안되네, 어떤 Rental 객체? 객체가 여러개가 있는 상황이 있는거야?
                .willAnswer(invocation -> invocation.getArgument(0)); // 인텔리제이에서 .willAnswer(InvocationOnMock::getArguments) 이렇게 하라는데 같은 의미야??

        // When : 실제 테스트할 메서드 호출
        RentalCreateRequest request = new RentalCreateRequest(
                1L,
                2L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 5),
                BigDecimal.valueOf(50000),
                "테스트 대여"
        );

        RentalResponse response = rentalService.create(request);

        // Then : 결과 검증
        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo("홍길동");
        assertThat(response.getCampingItemName()).isEqualTo("5인용 텐트");
        assertThat(response.getStatus()).isEqualTo(RentalStatus.PENDING);
        assertThat(response.getTotalCost()).isEqualTo(BigDecimal.valueOf(250000));

        verify(customerRepository, times(1)).findById(1L);
        verify(campingItemRepository, times(1)).findById(2L);
        verify(rentalRepository, times(1)).save(any(Rental.class));
    }

    @Test
    @DisplayName("대여 신청 실패 - 존재하지 않는 고객")
    void create_Fail_customerNotFound() {
        given(customerRepository.findById(999L)).willReturn(Optional.empty());
        
        // When & Then : 예외 발생하는지 검증
        RentalCreateRequest request = new RentalCreateRequest(
                999L,
                2L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 5),
                BigDecimal.valueOf(50000),
                "테스트"
        );

        assertThatThrownBy(() -> rentalService.create(request))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("존재하지 않는 고객입니다.");

        // 예외 발생으로 save()는 호출되지 않음
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    @DisplayName("대여 승인 성공")
    void approve_Success() {
        // Given : rental을 Mock Repository에서 반환하도록 설정
        given(rentalRepository.findById(1L)).willReturn(Optional.of(rental));

        // When
        RentalResponse response = rentalService.approve(1L);

        // Then : 상태가 APPROVED로 변경되었는지 확인
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(RentalStatus.APPROVED);

        // save() 호출 필요 없음 : rental.approve()는 엔티티의 상태만 변경. 더티 체킹으로 자동 UPDATE
        verify(rentalRepository, times(1)).findById(1L);
        //times(1)은 무슨 의미야?
    }

    @Test
    @DisplayName("대여 시작 성공 - 재고 감소 확인")
    void start_Success() {
        // Given
        given(rentalRepository.findById(1L)).willReturn(Optional.of(rental));

        // campingItem의 초기 재고 확인
        int initialStock = campingItem.getStockQuantity();

        // When: 대여 시작
        RentalResponse response = rentalService.start(1L);

        // Then : 상태 변경 + 재고 감소 확인
        assertThat(response.getStatus()).isEqualTo(RentalStatus.IN_PROGRESS);

        // 재고 검증( 10 -> 9개로 줄어들어야 함)
        assertThat(campingItem.getStockQuantity()).isEqualTo(initialStock -1);
    }


    @Test
    @DisplayName("반납 처리 성공 - 재고 복구 확인")
    void complete_Success() {
        // Given : 대여중 상태로 설정
        rental.start(); // PENDING -> APPROVED -> IN_PROGRESS

        campingItem.decreaseStock(1);

        given(rentalRepository.findById(1L)).willReturn(Optional.of(rental));

        int stockBeforeComplete = campingItem.getStockQuantity();

        // When : 반납 처리
        LocalDate returnDate = LocalDate.of(2026,1,5);
        RentalResponse response = rentalService.complete(1L, returnDate);

        // Then : 상태 변경 + 재고 복구 확인
        assertThat(response.getStatus()).isEqualTo(RentalStatus.COMPLETED);
        assertThat(response.getActualReturnDate()).isEqualTo(returnDate);
        assertThat(campingItem.getStockQuantity()).isEqualTo(stockBeforeComplete + 1);
    }

    @Test
    @DisplayName("대여 취소 - 대여중이었을 때 재고 복구")
    void cancel_Success_WithStockRecovery() {
        // Given : 대여중 상태
        rental.approve();   // PENDING -> APPROVED
        rental.start();     // APPROVED -> IN_PROGRESS
        campingItem.decreaseStock(1);   // 재고 10 -> 9

        given(rentalRepository.findById(1L)).willReturn(Optional.of(rental));

        // When : 취소
        RentalResponse response = rentalService.cancel(1L);

        // Then : 취소됨 + 재고 복구 됨
        assertThat(response.getStatus()).isEqualTo(RentalStatus.CANCELLED);
        assertThat(campingItem.getStockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("대여 취소 - PENDING 상태일 때는 재고 변화 없음")
    void cancel_Success_NoStockChange() {
        given(rentalRepository.findById(1L)).willReturn(Optional.of(rental));

        int initialStock = campingItem.getStockQuantity();

        // When : 취소
        RentalResponse response = rentalService.cancel(1L);

        // Then : 취소됨 + 재고 변화 없음
        assertThat(response.getStatus()).isEqualTo(RentalStatus.CANCELLED);
        assertThat(campingItem.getStockQuantity()).isEqualTo(initialStock);
    }

}
