package com.rental.camprent.domain.campingrental;

import com.rental.camprent.domain.campingitem.CampingItem;
import com.rental.camprent.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 대여 엔티티
 */
@Entity
@Table(name = "rentals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== JPA 관계 매핑 시작 =====
    @ManyToOne(fetch = FetchType.LAZY)  // 다대일, 지연 로딩
    @JoinColumn(name = "machine_id", nullable = false)
    private CampingItem machine;            // 대여 기계

    @ManyToOne(fetch = FetchType.LAZY)  // 다대일, 지연 로딩
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;                // 대여자(빌리는 사람)
    // ===== JPA 관계 매핑 끝 =====

    @Column(nullable = false)
    private LocalDate startDate;        // 대여 시작일

    @Column(nullable = false)
    private LocalDate endDate;          // 대여 종료일

    private LocalDate actualReturnDate; // 실제 반납일

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;       // 총 대여 비용

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deposit;         // 보증금

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus status;        // 대여 상태

    @Column(length = 500)
    private String notes;               // 비고(특이사항)

    @Column(updatable = false)
    private LocalDateTime createdAt;    // 신청일시

    private LocalDateTime updatedAt;    // 수정일시

    @Builder
    public Rental(CampingItem machine, User renter, LocalDate startDate,
                  LocalDate endDate, BigDecimal deposit, String notes) {
        this.machine = machine;
        this.renter = renter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deposit = deposit;
        this.notes = notes;
        this.status = RentalStatus.PENDING;  // 기본값: 대여신청
        this.totalCost = calculateTotalCost(machine, startDate, endDate);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 비즈니스 메서드 시작 =====
    /**
     *  총 비용 계산 (일일 대여료 * 대여 일 수)
     */
    private BigDecimal calculateTotalCost(CampingItem machine, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;    // 시작일 포함
        return machine.getBaseDailyRate().multiply(BigDecimal.valueOf(days));
    }

    /**
     * 대여 승인
     */
    public void approve() {
        if (this.status != RentalStatus.PENDING && this.status != RentalStatus.DEPOSIT_PENDING) {
            throw new IllegalStateException("대여신청 또는 보증금 대기 상태에서만 승인 가능합니다.");
        }
        this.status = RentalStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 대여 시작 (실제 대여 진행)
     */
    public void start() {
        if (this.status != RentalStatus.APPROVED) {
            throw new IllegalStateException("승인된 상태에서만 대여를 시작할 수 있습니다.");
        }
        this.status = RentalStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반납 처리
     */
    public void complete(LocalDate returnDate) {
        if (this.status != RentalStatus.IN_PROGRESS && this.status != RentalStatus.OVERDUE) {
            throw new IllegalStateException("대여중 또는 연체중 상태에서만 반납 가능합니다.");
        }
        this.actualReturnDate = returnDate;
        this.status = RentalStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 대여 취소
     */
    public void cancel() {
        if (this.status == RentalStatus.COMPLETED || this.status == RentalStatus.RETURED_DAMAGED)
        {
            throw new IllegalStateException("이미 완료된 대여는 취소할 수 없습니다.");
        }
        this.status = RentalStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 연체 상태로 변경
     */
    public void markAsOverdue() {
        if (this.status == RentalStatus.IN_PROGRESS) {
            this.status = RentalStatus.OVERDUE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 대여 기간 연장
     */
    public void extend(LocalDate newEndDate) {
        if (this.status != RentalStatus.IN_PROGRESS) {
            throw new IllegalStateException("대여중인 상태에서만 연장 가능합니다.");
        }
        if (newEndDate.isBefore(this.endDate)) {
            throw new IllegalArgumentException("연장 날짜는 기존 종료일 이후여야 합니다.");
        }
        this.endDate = newEndDate;
        this.totalCost = calculateTotalCost(this.machine, this.startDate, newEndDate);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 연체 여부 확인
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(this.endDate)
                && (this.status == RentalStatus.IN_PROGRESS || this.status ==
                RentalStatus.OVERDUE);
    }

    /**
     * 대여 일수 계산
     */
    public long getRentalDays() {
        LocalDate end = (actualReturnDate != null) ? actualReturnDate : endDate;
        return ChronoUnit.DAYS.between(startDate, end) + 1;
    }
}
