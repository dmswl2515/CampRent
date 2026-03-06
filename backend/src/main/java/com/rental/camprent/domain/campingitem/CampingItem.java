package com.rental.camprent.domain.campingitem;

import com.rental.camprent.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 캠핑 장비 엔티티
 */
@Entity
@Table(name = "camping_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;                // 장비명 (예 : 4인용 돔 텐트, 구스다운 침낭)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;                 // 장비 소유자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CampingCategory category;   // 캠핑 카테고리

    @Column(length = 100)
    private String model;               // 모델명/브랜드

    @Column(length = 500)
    private String description;         // 장비 설명

    @Column(nullable = false)
    private Integer stockQuantity;      // 재고 수량

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseDailyRate;   // 기본 일일 대여료 (비성수기 기준)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampingItemStatus status;    // 장비 상태

    @Column(updatable = false)
    private LocalDateTime createdAt;    // 등록일시

    private LocalDateTime updatedAt;    // 수정일시

    @Builder
    public CampingItem(String name, User owner, CampingCategory category, String model, String description,
                       Integer stockQuantity, BigDecimal baseDailyRate, CampingItemStatus status) {
        this.name = name;
        this.owner = owner;
        this.category = category;
        this.model = model;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.baseDailyRate = baseDailyRate;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 비즈니스 메서드 (Setter 대신) =====
    /**
     *  장비 정보 수정
     */
    public void updateInfo(String name, String model, String description, BigDecimal baseDailyRate) {
        this.name = name;
        this.model = model;
        this.description = description;
        this.baseDailyRate = baseDailyRate;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 상태 변경
     */
    public void updateStatus(CampingItemStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재고 증가
     */
    public void increaseStock(int quantity) {
        if(quantity <= 0) {
            throw new IllegalArgumentException("재고 수량은 0보다 커야 증가할 수 있습니다.");
        }
        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재고 감소
     */
    public void decreaseStock(int quantity) {
        if(quantity <= 0) {
            throw new IllegalArgumentException("재고 수량은 0보다 커야 감소할 수 있습니다.");
        }
        if(this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 대여 가능 여부 확인
     */
    public boolean isAvailable(int requestedQuantity) {
        return this.status == CampingItemStatus.AVAILABLE && this.stockQuantity >= requestedQuantity;
    }


}

