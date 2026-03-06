# CampRent - 캠핑 장비 대여 플랫폼 개발 가이드라인

## 프로젝트 개요

### 프로젝트명
**CampRent** - 동네 기반 캠핑 장비 P2P 대여 플랫폼

### 목적
- **포트폴리오**: 백엔드 개발자 역량 증명 (복잡한 도메인 로직)
- **기술 학습**: 최신 기술 스택과 실무 패턴 학습
- **실제 서비스 가능성**: MVP 이후 실제 런칭 검토 (당근마켓 스타일 P2P)

### 서비스 방향 (2026-03-06 확정)
- **B2C → P2P 전환**: 관리자 운영 방식에서 사용자 간 직거래 방식으로 변경
- 사용자가 자신의 캠핑 장비를 직접 등록하고 가격 설정
- 동네 기반으로 근처 장비 검색 및 대여
- 빌려주는 사람 / 빌리는 사람 모두 동일한 User

### 비즈니스 요구사항
1. 캠핑 장비 카테고리별 관리 (텐트, 침낭, 버너 등)
2. **사용자가 직접 가격 설정** (P2P 방식, 계절별 가격 정책 불필요)
3. 대여 정보 관리 (대여자, 기간, 비용, 보증금)
4. **동네 기반 검색** (User 프로필의 neighborhood 기준)
5. **날씨 기반 추천** (외부 API 연동, Phase 2)
6. **후기/평점 시스템** (신뢰도, Phase 2)

---

## 기술 스택

### Backend (현재)
- **Java 21** (LTS)
- **Spring Boot 4.x**
- **Spring Security** (JWT 인증)
- **Spring Data JPA**
- **PostgreSQL** (운영), **H2** (개발)
- **Redis** (캐싱, 실시간 예약 현황)
- **Spring Boot Actuator** (모니터링)
- **Gradle**
- **Lombok** (보일러플레이트 코드 제거)

### Frontend (예정)
- **React 18 + TypeScript**
- **Tailwind CSS**
- **React Query** (서버 상태 관리)
- **Vite** (빌드 도구)

### 외부 API 연동
- **날씨 API**: 기상청 API 또는 OpenWeatherMap
- **지도 API**: 카카오맵 또는 Google Maps
- **결제 API**: 토스페이먼츠 또는 아임포트 (Phase 3)

### 배포
- **Docker Compose** (개발 환경)
- **Railway/Render** (백엔드)
- **Vercel** (프론트엔드)

---

## 데이터베이스 설계

### 핵심 엔티티

#### CampingItem (캠핑 장비)
- id, name, category, brand, dailyRate, peakSeasonRate, status
- **category**: CampingCategory Enum (텐트, 침낭, 버너 등)
- **peakSeasonRate**: 성수기 가격
- **status**: 사용가능/대여중/수리중/재고없음

#### Customer (고객)
- id, name, phone, email, type (개인/기업), address
- **campingLevel**: 초보/중급/고급 (추천 시스템용)

#### CampingRental (대여)
- id, campingItem(FK), customer(FK), startDate, endDate
- totalCost, deposit, status, isPackageRental
- **appliedSeason**: 적용된 계절 (가격 계산용)

#### User (시스템 사용자)
- id, username, password, role (관리자/일반사용자)

#### CampingPackage (패키지 상품)
- id, name, description, discountRate
- **packageItems**: List<PackageItem> (구성 아이템)

#### PackageItem (패키지 구성)
- id, package(FK), campingItem(FK), quantity

### Enum 정의

#### CampingCategory (캠핑 카테고리)
```java
TENT("텐트"),
SLEEPING_BAG("침낭"),
CAMP_STOVE("버너/스토브"),
CAMPING_FURNITURE("캠핑 가구"),
COOKING_GEAR("조리용품"),
LIGHTING("조명"),
RAIN_GEAR("우천용품"),
COOLER("아이스박스/쿨러")
```

#### Season (계절)
```java
SPRING("봄", false),
SUMMER("여름", true),    // 성수기
AUTUMN("가을", false),
WINTER("겨울", false)
```

#### CampingItemStatus (장비 상태)
```java
AVAILABLE("대여가능"),
RENTED("대여중"),
UNDER_REPAIR("수리중"),
OUT_OF_STOCK("재고없음")
```

#### RentalStatus (대여 상태)
```java
PENDING("대여신청"),
APPROVED("승인됨"),
IN_PROGRESS("대여중"),
COMPLETED("반납완료"),
CANCELLED("취소됨"),
OVERDUE("연체중"),
DEPOSIT_PENDING("보증금대기"),
RETURNED_DAMAGED("파손반납")
```

---

## 개발 단계

### Phase 1: MVP (현재 진행 중)

#### Week 1-2: 기본 도메인 모델
- [x] 프로젝트 초기 설정
- [x] 모노레포 구조 구성
- [x] 기본 Entity 작성 (Machine, Customer, Rental, User)
- [ ] 새 Enum 추가 (CampingCategory, Season, CampingItemStatus)
- [ ] 새 Entity 작성 (CampingItem, CampingRental)

#### Week 3-4: 핵심 비즈니스 로직
- [ ] Repository 계층 구현
- [ ] **계절별 가격 정책** 구현
- [ ] 기본 대여 관리 API
- [ ] 검색 API (카테고리, 이름, 재고 상태)

#### Week 5-6: 패키지 시스템
- [ ] CampingPackage Entity 추가
- [ ] 패키지 할인 로직 구현
- [ ] 패키지 추천 API

#### Week 7-8: 프론트엔드 기본
- [ ] 캠핑 장비 검색 화면
- [ ] 대여 신청 화면
- [ ] 관리자 대시보드

### Phase 2: 고급 기능
- [ ] **날씨 API 연동** (날씨 기반 장비 추천)
- [ ] 캠핑장 지도 연동
- [ ] 실시간 재고 현황 (Redis)
- [ ] 이메일/SMS 알림
- [ ] 캠핑 초보자 가이드 페이지
- [ ] 리뷰 시스템

### Phase 3: 확장
- [ ] 모바일 앱 (React Native)
- [ ] AI 기반 개인화 추천
- [ ] 결제 시스템 연동
- [ ] 고급 통계 및 리포트
- [ ] 다양한 아웃도어 활동으로 확장 (등산, 낚시 등)

---

## 핵심 비즈니스 로직

### 1. 계절별 가격 정책
```java
// CampingItem Entity
public BigDecimal getSeasonalPrice(LocalDate rentalDate) {
    Season currentSeason = Season.fromDate(rentalDate);

    if (currentSeason == Season.SUMMER) {
        return peakSeasonRate;  // 성수기 가격
    }
    return dailyRate;  // 일반 가격
}
```

### 2. 패키지 할인
```java
// CampingPackage Entity
public BigDecimal calculateDiscountedPrice() {
    BigDecimal totalIndividual = packageItems.stream()
        .map(item -> item.getCampingItem().getDailyRate()
                         .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return totalIndividual.multiply(BigDecimal.ONE.subtract(discountRate));
}
```

### 3. 날씨 기반 추천 (Phase 2)
```java
// RecommendationService
public List<CampingItem> getWeatherBasedRecommendation(String location, LocalDate date) {
    Weather weather = weatherApiClient.getWeather(location, date);

    if (weather.isRainy()) {
        return itemRepository.findByCategory(CampingCategory.RAIN_GEAR);
    } else if (weather.isCold()) {
        return itemRepository.findByCategory(CampingCategory.SLEEPING_BAG);
    }
    // 날씨별 추천 로직
}
```

### 4. 캠핑 레벨별 추천
```java
// RecommendationService
public CampingPackage recommendPackageByLevel(CampingLevel level) {
    if (level == CampingLevel.BEGINNER) {
        return packageRepository.findByName("캠핑 입문 패키지");
    }
    // 레벨별 패키지 추천
}
```

---

## 보안 & 아키텍처

### 보안
- JWT 토큰 인증
- 역할 기반 권한 (ADMIN/USER)
- CORS 설정
- 입력값 검증, SQL Injection 방지
- BCrypt 비밀번호 암호화
- 외부 API Key 환경변수 관리

### 아키텍처
- **레이어드 아키텍처**: Controller → Service → Repository
- **도메인 주도 설계 (DDD)** 패턴 적용
- **Rich Domain Model**: Entity에 비즈니스 로직
- RESTful API 설계
- 캐싱 전략 (Redis)
- 페이징, 인덱싱 최적화

---

## 개발 가이드

### 추천 개발 순서
1. **도메인 모델**: Entity, Enum 정의
2. **Repository**: JPA Repository 구현
3. **Service**: 비즈니스 로직 (계절별 가격, 패키지 할인)
4. **Controller**: REST API
5. **Security**: 인증/인가
6. **Exception Handler**: 전역 예외 처리
7. **Validation**: 검증 로직
8. **Test**: 단위/통합 테스트
9. **외부 API 연동**: 날씨 API

### 코드 작성 원칙
- 단계별로 하나씩 구현 (한 번에 하나의 기능)
- 테스트 주도 개발 권장
- 명확한 네이밍 (한글 주석 허용)
- 비즈니스 로직은 **Entity에 먼저**, 복잡하면 Service로
- Controller는 얇게 유지
- 외부 API는 별도 Client 클래스로 분리

---

## 포트폴리오 어필 포인트

### 기술적 도전
1. **복잡한 도메인 로직**: 계절성 가격, 패키지 시스템
2. **외부 API 연동**: 날씨 API, 지도 API
3. **실시간 시스템**: Redis를 활용한 실시간 재고 현황
4. **추천 알고리즘**: 날씨, 캠핑 레벨 기반 추천
5. **확장 가능한 설계**: 다양한 아웃도어 활동으로 확장 가능

### 비즈니스 이해도
1. **트렌드 반영**: 급성장하는 캠핑 시장
2. **실무 적합성**: 실제 서비스로 런칭 가능
3. **사용자 경험**: 캠핑 초보자 배려 (가이드, 추천)

---

## 개발 시 참고사항

### 사용자 의도
- 포트폴리오용이므로 **Best Practice** 적용 중요
- **직접 타이핑하며 공부**: 코드를 제공하면 사용자가 직접 따라 치며 학습
- 확장 가능한 구조 - **모듈화, 재사용성**
- 기술적 도전 과제 포함 (외부 API, 복잡한 로직)

### 응답 방식 (CRITICAL - 반드시 준수)
1. **개념 먼저, 코드 나중**: 코드를 보여주기 전에 반드시 개념과 이유를 먼저 설명
2. **한 단계씩**: 한 번에 하나의 파일 또는 하나의 개념만 진행
3. **질의응답 방식**: 각 단계 후 사용자의 확인/질문을 기다린 후 다음 단계 진행
4. **절대 한꺼번에 많은 양 제공 금지**: 정보 과부하 주의
5. 코드 작성 이유 설명 (왜 이렇게 작성했는지)
6. 실무 패턴과 이유 설명
7. 대안이 있으면 함께 제시
8. 새로운 개념은 예시와 함께 설명

### 리팩토링 시 주의사항
- 기존 코드 구조 최대한 활용
- 한 번에 하나씩 변경 (파일별 단계적 진행)
- 변경 전후 비교 설명
- 테스트로 검증

---

## P2P 전환 작업 현황 (2026-03-06 시작)

### 전환 배경
B2C(관리자 운영) → P2P(사용자 간 직거래) 방향으로 재설계.
당근마켓처럼 내 동네 사람에게 캠핑 장비를 빌려주는 서비스.

### 설계 결정사항
- 위치 정보: **User 프로필**에 `neighborhood` (동네명) 저장
- 수량: 게시글 1개에 `stockQuantity`로 관리 (2개 보유 → 수량=2)
- Customer 엔티티 **삭제** → User로 통합
- `CampingItem.owner` (User FK) 추가 - 장비 소유자
- `Rental.renter` (User FK) - Customer 대신 User
- `Season.java` 삭제 (P2P는 사용자가 직접 가격 설정)

### P2P 전환 단계별 진행 상황
- [ ] Step 1: `User.java` - `neighborhood`, `latitude`, `longitude` 필드 추가
- [ ] Step 2: `CampingItem.java` - `owner` (User FK) 추가, `calculateDailyRate()` 제거
- [ ] Step 3: `Rental.java` - `customer` → `renter` (User FK) 교체
- [ ] Step 4: Customer 관련 파일 전체 삭제 (Customer, CustomerType, CustomerRepository, CustomerService, CustomerController, 관련 DTO)
- [ ] Step 5: `RentalService.java` - `customerRepository` → `userRepository` 교체
- [ ] Step 6: DTO 수정 (RentalCreateRequest, RentalResponse)
- [ ] Step 7: `V1__Init_schema.sql` 수정 (초기 스키마라 직접 수정)
- [ ] Step 8: `RentalServiceTest.java` - Customer → User로 테스트 데이터 교체

---

**작성일**: 2025-12-19
**최종 업데이트**: 2026-03-06
**현재 상태**: P2P 전환 시작 전 (Step 1 대기 중)
**다음 단계**: Step 1 - User 엔티티에 위치 정보 추가
