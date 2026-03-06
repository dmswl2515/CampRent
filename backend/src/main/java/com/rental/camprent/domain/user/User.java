package com.rental.camprent.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티 (로그인 계정)
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;                            // 로그인 아이디

    @Column(nullable = false)
    private String password;                            // 비밀번호(암호화)

    @Column(nullable = false, length = 50)
    private String name;                                // 실명

    @Column(nullable = false, unique = true, length = 100)
    private String email;                               // 이메일

    @Column(length =20)
    private String phone;

    @Column(length = 100)
    private String neighborhood;                        // 동네명(예 : 서초동)

    private Double latitude;                            // 위도

    private Double longitude;                           // 경도

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;                              // 권한 (ADMIN / USER)

    @Column(nullable = false)
    private boolean enabled;                            // 계정 활성화 여부

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;                    // 가입일시

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;                    // 수정일시

    private LocalDateTime lastLoginAt;                  // 마지막 로그인 시간

    @Builder
    public User(String username, String password, String name,
                String email, String phone, UserRole role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.enabled = true;      // 기본값: 활성화
    }

    // 비즈니스 메서드
    // 개인정보 수정 메서드
    public void updateInfo(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // 비밀번호 변경 메서드 (암호화된 비밀번호로 변경)
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // 마지막 로그인 시간 업데이트
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // 동네 설정
    public void updateNeighborhood(String neighborhood, Double latitude, Double longitude) {
        this.neighborhood = neighborhood;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

}
