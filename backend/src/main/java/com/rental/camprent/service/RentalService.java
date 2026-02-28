package com.rental.camprent.service;

import com.rental.camprent.domain.campingitem.CampingItem;
import com.rental.camprent.domain.campingitem.CampingItemRepository;
import com.rental.camprent.domain.campingrental.Rental;
import com.rental.camprent.domain.campingrental.RentalRepository;
import com.rental.camprent.domain.campingrental.RentalStatus;
import com.rental.camprent.domain.customer.Customer;
import com.rental.camprent.domain.customer.CustomerRepository;
import com.rental.camprent.dto.request.RentalCreateRequest;
import com.rental.camprent.dto.response.RentalResponse;
import com.rental.camprent.exception.ItemNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CustomerRepository customerRepository;
    private final CampingItemRepository campingItemRepository;

    /**
     * 대여 신청
     * customerId와 campingItemId로 실제 엔티티를 조회
     * Rental 엔티티에 Customer, CampingItem 객체를 연결(JPA 연관관계)
     * 총 비용은 Rental 엔티티의 생성자에서 자동 계산됨
     * */
    @Transactional
    public RentalResponse create(RentalCreateRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 고객입니다."));

        // 캠핑 장비 조회
        CampingItem campingItem = campingItemRepository.findById(request.getCampingItemId())
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 캠핑 장비입니다."));

        // Rental 엔티티 생성 (상태는 자동으로 PENDING, 비용은 자동 계산)
        Rental rental = Rental.builder()
                .customer(customer)
                .machine(campingItem)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .deposit(request.getDeposit())
                .notes(request.getNotes())
                .build();

        // 저장
        Rental saveRental = rentalRepository.save(rental);
        return RentalResponse.from(saveRental);
    }

    /**
    * 대여 단건 조회
    * */
    public RentalResponse findById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 대여 정보입니다."));

        return RentalResponse.from(rental);
    }

    /**
     * 전체 대여 목록 조회
     * */
    public List<RentalResponse> findAll() {
        List<Rental> rentals = rentalRepository.findAll();

        return rentals.stream()
                .map(RentalResponse::from)
                .toList();
    }

    /**
     * 특정 고객의 대여 이력 조회
     * */
    public List<RentalResponse> findByCustomerId(Long customerId) {
        List<Rental> rentals = rentalRepository.findByCustomerId(customerId);
        return rentals.stream()
                .map(RentalResponse::from)
                .toList();
    }

    /**
     * 특정 캠핑 장비의 대여 이력 조회
     * */
    public List<RentalResponse> findByMachineId(Long machineId) {
        List<Rental> rentals = rentalRepository.findByMachineId(machineId);
        return rentals.stream()
                .map(RentalResponse::from)
                .toList();
    }

    /**
     * 상태별 대여 목록 조회
     * 예 : PENDING(대여신청), IN_PROGRESS(대여중), COMPLETED(반납완료)
     * */
    public List<RentalResponse> findByStatus(RentalStatus status) {
        List<Rental> rentals = rentalRepository.findByStatus(status);
        return rentals.stream()
                .map(RentalResponse::from)
                .toList();
    }

    // ===== 상태 전환 메서드 시작 =====

    /**
     * 대여 승인
     * PENDING -> APPROVED
     * */
    @Transactional
    public RentalResponse approve(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 대여 정보입니다."));

        rental.approve();   // 엔티티의 비즈니스 메서드
        return RentalResponse.from(rental);
    }

    /**
     * 대여 거부 (취소 처리)
     * PENDING -> CANCELLED
     * */
    @Transactional
    public RentalResponse reject(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 대여 정보입니다."));
        rental.cancel();
        return RentalResponse.from(rental);
    }

    /**
     * 대여 시작
     * APPROVED -> IN_PROGRESS
     * + 캠핑 장비 재고 1 감소
     * */
    @Transactional
    public RentalResponse start(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 대여 정보입니다."));

        CampingItem item = rental.getMachine();

        // 재고 감소
        item.decreaseStock(1);

        // 상태 변경
        rental.start();

        return RentalResponse.from(rental);
    }

    /**
     * 반납 처리
     * IN_PROGRESS/OVERDUE -> COMPLETED
     * + 캠핑 장비 재고 1개 복구
     * */
    @Transactional
    public RentalResponse complete(Long id, LocalDate returnDate) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 대여 정보입니다."));

        CampingItem item = rental.getMachine();

        // 재고 복구
        item.increaseStock(1);

        // 상태 변경
        rental.complete(returnDate);

        return RentalResponse.from(rental);
    }

    /**
     * 대여 취소
     * 모든 상태 -> CANCELLED
     * + 대여중(IN_PROGRESS)이었다면 재고 복구
     * */
    @Transactional
    public RentalResponse cancel(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 대여 정보입니다."));

        // 대여중이었다면 재고 복구
        if(rental.getStatus() == RentalStatus.IN_PROGRESS) {
            CampingItem item = rental.getMachine();
            item.increaseStock(1);
        }

        rental.cancel();
        return RentalResponse.from(rental);
    }
}
