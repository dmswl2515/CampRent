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
        // 리스트는 예외처리 안해?
        List<Rental> rentals = rentalRepository.findAll();

        //map(RentalResponse::from) 여기서 from은 뭔뜻이야? 그 정적 펙토리 메서드인건가?
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
}
