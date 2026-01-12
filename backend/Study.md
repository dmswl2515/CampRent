# 개발 지식 정리

## 목차

- [@Builder 어노테이션 정리](#builder-어노테이션-정리)
- [JPA 연관관계 매핑 정리](#jpa-연관관계-매핑-정리)
    - [@ManyToOne (다대일 관계)](#manytoone-다대일-관계)
    - [FetchType.LAZY (지연 로딩)](#fetchtypelazy-지연-로딩)
- [비즈니스 로직 위치: Entity vs Service](#비즈니스-로직-위치-entity-vs-service)
- [Validation 어노테이션 (@NotNull vs @NotBlank)](#validation-어노테이션-notnull-vs-notblank)
- [DTO (Data Transfer Object) 패턴](#dto-data-transfer-object-패턴)
- [Static Factory Method 패턴](#static-factory-method-패턴)
- [Controller 단위 테스트 (@WebMvcTest)](#controller-단위-테스트-webmvctest)
---

## @Builder 어노테이션 정리

**@Builder란:**
> **롬복(Lombok)에서 제공하는 애노테이션으로, 빌더 패턴(Builder Pattern)을 자동으로 구현해주는 도구**

### 기본 개념
```java
// 빌더 패턴: 복잡한 객체를 단계별로 생성하는 디자인 패턴
// 롬복 @Builder: 이 패턴을 자동으로 코드 생성해주는 어노테이션
```

### 동작 원리
```java
@Builder  // 이 어노테이션 하나로
public Customer(매개변수들...) { ... }

// 롬복이 자동으로 이런 코드들을 생성:
// 1. CustomerBuilder 클래스
// 2. builder() 정적 메서드  
// 3. 메서드 체이닝을 위한 setter들
// 4. build() 메서드 (실제 객체 생성)
```

### 사용법
```java
// 메서드 체이닝 방식으로 객체 생성
Customer customer = Customer.builder()
                .name("홍길동")           // 필드명.설정값 형태
                .phone("010-1234-5678")  // 순서 상관없이 설정
                .type(CustomerType.INDIVIDUAL)
                .build();                // 마지막에 build()로 객체 생성
```

## 결론

### @Builder 정의
**롬복에서 제공하는 애노테이션으로, 빌더 패턴을 자동 구현하여 객체를 메서드 체이닝 방식으로 생성할 수 있게 해주는 도구**

### @Builder를 생성자에 적용한 이유

1. **기본값 자동 설정** - createdAt, updatedAt이 항상 자동으로 설정됨
2. **선택적 매개변수** - 필요한 필드만 선택해서 설정 가능
3. **가독성** - 메서드 체이닝으로 읽기 쉬운 코드
4. **실수 방지** - 매개변수 순서 헷갈림 방지, null 체크 자동화
5. **유연성** - 개인/기업 고객 상황에 맞춰 다르게 생성 가능

### 핵심
> **빌더 패턴의 편리함 + 생성자의 초기화 로직 = 실무에서 선호하는 객체 생성 방식**

## 예시 코드

### 실제 사용 예시
```java
@Entity
public class Customer {
    // 필드들...

    @Builder
    public Customer(String name, String phone, String email, CustomerType type,
                    String address, String businessNumber) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.type = type;
        this.address = address;
        this.businessNumber = businessNumber;
        this.createdAt = LocalDateTime.now();      // 자동 설정
        this.updatedAt = LocalDateTime.now();      // 자동 설정
    }
}
```

### 다양한 생성 패턴
```java
// 개인 고객 생성 (최소 정보)
Customer individual = Customer.builder()
                .name("홍길동")
                .phone("010-1234-5678")
                .type(CustomerType.INDIVIDUAL)
                .build();

// 기업 고객 생성 (완전한 정보)
Customer business = Customer.builder()
        .name("ABC 건설")
        .phone("02-123-4567")
        .email("info@abc.co.kr")
        .type(CustomerType.BUSINESS)
        .address("서울시 중구")
        .businessNumber("123-45-67890")
        .build();
```
---

## JPA 연관관계 매핑 정리

### @ManyToOne (다대일 관계)
```java
@ManyToOne
@JoinColumn(name = "machine_id")
private Machine machine;
```

#### 의미
- 여러 개의 Rental → 하나의 Machine
- 한 기계가 여러 번 대여될 수 있음

#### 예시
```
굴착기(Machine)
  ← Rental #1 (2024-01-01 ~ 2024-01-10)
  ← Rental #2 (2024-02-01 ~ 2024-02-15)
  ← Rental #3 (2024-03-01 ~ 2024-03-20)
```

#### 데이터베이스 구조
```sql
-- rental 테이블
CREATE TABLE rental (
    id BIGINT PRIMARY KEY,
    machine_id BIGINT,          -- 외래키
    customer_id BIGINT,
    start_date DATE,
    end_date DATE,
    FOREIGN KEY (machine_id) REFERENCES machine(id)
);
```

### FetchType.LAZY (지연 로딩)
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "machine_id")
private Machine machine;
```

#### 의미
- Rental을 조회할 때 Machine 정보는 나중에 필요할 때 가져옴
- 성능 최적화

#### 예시

**LAZY 로딩 (추천)**
```java
// LAZY: Rental만 먼저 조회 (빠름)
Rental rental = rentalRepository.findById(1L);

// Machine이 필요할 때 그때 DB 조회
String machineName = rental.getMachine().getName();  // 이때 DB 조회
```

**EAGER 로딩 vs LAZY 로딩 비교**
```java
// EAGER: 항상 Machine까지 함께 조회 (무거움)
@ManyToOne(fetch = FetchType.EAGER)  // 사용 비추천
private Machine machine;

// LAZY: 필요할 때만 조회 (가벼움) 
@ManyToOne(fetch = FetchType.LAZY)   // 추천
private Machine machine;
```

#### 실무 활용 예시
```java
// 대여 목록 조회 (Machine 정보 불필요)
List rentals = rentalRepository.findAll();
for (Rental rental : rentals) {
    System.out.println("대여 ID: " + rental.getId());
    // Machine 정보는 조회하지 않음 → 빠른 성능
}

// 특정 대여의 상세 정보 (Machine 정보 필요)
Rental rental = rentalRepository.findById(1L);
System.out.println("기계명: " + rental.getMachine().getName());  // 이때 DB 조회
```

### @JoinColumn 외래키 설정
```java
@JoinColumn(name = "machine_id")
private Machine machine;
```

#### 데이터베이스 테이블 구조
```sql
-- machine 테이블 (부모 테이블)
CREATE TABLE machine (
    id BIGINT PRIMARY KEY,          -- 기본키
    name VARCHAR(100),
    model VARCHAR(50),
    daily_rate DECIMAL(10,2)
);

-- rental 테이블 (자식 테이블)  
CREATE TABLE rental (
    id BIGINT PRIMARY KEY,          -- 기본키
    machine_id BIGINT,              -- 외래키 ← 여기가 핵심
    customer_id BIGINT,
    start_date DATE,
    end_date DATE,
    total_amount DECIMAL(12,2),
    
    FOREIGN KEY (machine_id) REFERENCES machine(id)  -- 외래키 제약 조건
);
```

#### @JoinColumn의 의미
```
@JoinColumn(name = "machine_id")의 뜻:

1. "rental 테이블에 machine_id라는 컬럼을 만들어라"
2. "이 컬럼은 machine 테이블의 id를 참조하는 외래키다"
3. "Rental 객체의 machine 필드와 이 컬럼을 연결해라"
```

#### 외래키가 필요한 이유

**1. 데이터 무결성 보장**
```sql
-- 올바른 데이터 입력
INSERT INTO rental (id, machine_id, customer_id, start_date) 
VALUES (1, 100, 200, '2024-01-01');  -- machine_id = 100이 machine 테이블에 존재

-- 잘못된 데이터 입력 시도
INSERT INTO rental (id, machine_id, customer_id, start_date)
VALUES (2, 999, 200, '2024-01-01');  -- machine_id = 999가 존재하지 않음

-- 결과: 외래키 제약 조건 위반 에러
-- ERROR: Foreign key constraint violation
```

**2. 참조 무결성 유지**
```sql
-- 이런 상황을 방지
-- machine 테이블에서 기계 삭제 시도
DELETE FROM machine WHERE id = 100;

-- 하지만 rental 테이블에서 machine_id = 100인 대여 기록이 있으면?
-- 외래키가 있으면: 삭제 불가 (참조 무결성 보장)
-- 외래키가 없으면: 삭제됨 (고아 레코드 발생)
```

#### 실제 동작 예시

**Java 코드에서 사용**
```java
// 대여 생성
Rental rental = Rental.builder()
    .machine(existingMachine)  // Machine 객체 할당
    .customer(existingCustomer)
    .startDate(LocalDate.now())
    .endDate(LocalDate.now().plusDays(7))
    .build();

rentalRepository.save(rental);

// JPA가 실행하는 SQL:
// INSERT INTO rental (id, machine_id, customer_id, start_date, end_date) 
// VALUES (1, 100, 200, '2024-01-01', '2024-01-08');
//            ↑ machine.getId() 값이 자동으로 들어감
```

### 결론

#### @ManyToOne 핵심
- **다대일 관계 표현** - 여러 대여가 하나의 기계를 참조
- **외래키 관리** - @JoinColumn으로 데이터베이스 연결
- **객체 지향적 접근** - rental.getMachine()으로 연관된 객체에 쉽게 접근

#### FetchType.LAZY 핵심
- **성능 최적화** - 필요한 데이터만 조회
- **N+1 문제 방지** - 불필요한 쿼리 실행 방지
- **실무 표준** - 대부분의 연관관계에서 LAZY 사용 권장

#### @JoinColumn 핵심
- **데이터 무결성** - 존재하지 않는 기계에 대한 대여 생성 방지
- **참조 무결성** - 기계가 삭제되면 관련 대여 처리 방식 제어
- **비즈니스 규칙** - "대여는 반드시 특정 기계에 속해야 함" 규칙 강제

## 비즈니스 로직 위치: Entity vs Service

### 두 가지 설계 방식

#### 1. Rich Domain Model (도메인 모델 패턴) - Entity에 로직
```java
// Entity
public class Rental {
    public void approve() {
        if (this.status = RentalStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태가 아닙니다");
        }
        this.status = RentalStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }
}

// Service (얇게 유지)
public class RentalService {
    public void approveRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId);
        rental.approve();  // ← Entity의 메서드 호출
        rentalRepository.save(rental);
    }
}
```

#### 2. Anemic Domain Model (빈약한 도메인 모델) - Service에 로직
```java
// Entity (getter/setter만)
public class Rental {
    // 비즈니스 로직 없음, getter/setter만
}

// Service (두꺼움)
public class RentalService {
    public void approveRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId);
        
        // 모든 로직이 Service에
        if (rental.getStatus() = RentalStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태가 아닙니다");
        }
        rental.setStatus(RentalStatus.APPROVED);
        rental.setUpdatedAt(LocalDateTime.now());
        
        rentalRepository.save(rental);
    }
}
```

### 비교표

| 항목 | Rich Domain Model (Entity에) | Anemic Model (Service에) |
|------|------------------------------|--------------------------|
| 로직 위치 | Entity | Service |
| Entity 역할 | 데이터 + 행동 | 데이터만 |
| Service 역할 | 조율만 (얇음) | 모든 로직 처리 (두꺼움) |
| 장점 | 객체지향적, 재사용성 높음, 캡슐화 | 이해하기 쉬움, 단순함 |
| 단점 | 복잡할 수 있음 | Entity가 데이터 덩어리, 중복 발생 가능 |
| 적합한 경우 | 복잡한 도메인 로직 | 단순한 CRUD |

### 언제 어디에 둘까?

#### Entity에 두는 것이 좋은 경우

**1. 단일 Entity의 상태 변경**
```java
// Entity에
public void approve() {
    if (this.status = RentalStatus.PENDING) {
        throw new IllegalStateException("승인 대기 상태가 아닙니다");
    }
    this.status = RentalStatus.APPROVED;
    this.updatedAt = LocalDateTime.now();
}
```

**2. 유효성 검증**
```java
// Entity에
public void extend(LocalDate newEndDate) {
    if (newEndDate.isBefore(this.endDate)) {
        throw new IllegalArgumentException("연장일은 기존 종료일보다 늦어야 합니다");
    }
    this.endDate = newEndDate;
    this.updatedAt = LocalDateTime.now();
}
```

**3. 계산 로직**
```java
// Entity에
public long getRentalDays() {
    return ChronoUnit.DAYS.between(startDate, endDate) + 1;
}
```

#### Service에 두는 것이 좋은 경우

**1. 여러 Entity를 조합**
```java
//  Service에
public void createRental(CreateRentalRequest request) {
    Machine machine = machineRepository.findById(request.getMachineId());
    Customer customer = customerRepository.findById(request.getCustomerId());

    // Machine 상태도 변경
    machine.updateStatus(MachineStatus.RENTED);

    // Rental 생성
    Rental rental = Rental.builder()
        .machine(machine)
        .customer(customer)
        .build();

    rentalRepository.save(rental);
}
```

**2. 외부 서비스 호출**
```java
// Service에
public void completeRental(Long rentalId) {
    Rental rental = rentalRepository.findById(rentalId);
    rental.complete(LocalDate.now());

    // 이메일 발송 (외부 서비스)
    emailService.sendReturnConfirmation(rental);

    // SMS 발송 (외부 서비스)
    smsService.sendReturnNotification(rental);
}
```

**3. 트랜잭션 관리**
```java
// Service에
@Transactional
public void cancelRentalAndRefund(Long rentalId) {
    Rental rental = rentalRepository.findById(rentalId);
    rental.cancel();

    // 보증금 환불 처리
    paymentService.refund(rental.getDeposit());
}
```

### 실무 권장 패턴

**Entity: 자기 자신에 대한 로직**
```java
@Entity
public class Rental {
    // 상태 변경
    public void approve() { ... }

    // 유효성 검증
    public void extend(LocalDate newEndDate) { ... }

    // 계산
    public long getRentalDays() { ... }
}
```

**Service: 여러 객체 조율, 외부 연동**
```java
@Service
public class RentalService {
    // 여러 Entity 조합
    public void createRental(CreateRentalRequest request) {
        Machine machine = ...;
        Customer customer = ...;
        Rental rental = Rental.builder()...;
    }

    // 외부 서비스 호출
    public void completeWithNotification(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId);
        rental.complete(LocalDate.now());  // ← Entity 메서드
        emailService.send(...);  // ← Service에서 처리
    }
}
```

### 이론적 배경

이것이 바로 DDD(Domain-Driven Design, 도메인 주도 설계)의 핵심 개념입니다

- **Martin Fowler**: "Rich Domain Model을 권장"
- **Spring 공식**: 두 가지 모두 지원하지만 Rich Model 선호
- **실무**: 프로젝트 복잡도에 따라 선택

### 핵심 원칙
> **"Entity는 자신의 상태와 행동을 책임지고, Service는 여러 Entity를 조율한다"**

---

## Validation Annotation (@NotNull vs @NotBlank)

### 기본 개념

**Validation이란?**
> 클라이언트가 보낸 데이터가 올바른지 검증하는 과정

**주요 어노테이션:**
- `@NotNull`: null 값만 체크(빈 문자열은 허용)
- `@NotBlank`: null, 빈 문자열(""), 공백(" ")만으로 이루어진 문자열 모두 체크
- `@NotEmpty`: null, 빈 문자열("") 체크 (공백만 있는 문자열은 허용)

### 차이점 비교

#### 각 어노테이션의 검증 범위

```java
// 테스트 값들
String value1 = null;           // null
String value2 = "";             // 빈 문자열
String value3 = "   ";          // 공백만
String value4 = "4인용 텐트";    // 정상 값 (단어 사이 공백 포함)
```

**비교표:**

| 값 | @NotNull | @NotEmpty | @NotBlank |
|---|----------|-----------|-----------|
| `null` | 실패 | 실패 | 실패 |
| `""` (빈 문자열) | 통과 | 실패 | 실패 |
| `"   "` (공백만) | 통과 | 통과 | 실패 |
| `"4인용 텐트"` | 통과 | 통과 | 통과 |

### 실무 사용 가이드

#### String 필드 → @NotBlank 사용

```java
@NotBlank  // 가장 엄격한 검증
private String name;

@NotBlank
private String description;

// 잘못된 예시
@NotNull   // 빈 문자열 "" 허용됨 (너무 약함)
private String name;
```

**이유:**
- 사용자가 이름을 공백으로만 입력하는 것 방지
- `"   "` 같은 무의미한 데이터 차단

#### Enum/숫자 필드 → @NotNull 사용

```java
@NotNull  // Enum은 @NotNull만 사용
private CampingCategory category;

@NotNull  // 숫자도 @NotNull
private Integer stockQuantity;

@NotNull
private BigDecimal baseDailyRate;

// 잘못된 예시
@NotBlank  // Enum이나 숫자에는 사용 불가 (컴파일 에러)
private CampingCategory category;
```

**이유:**
- @NotBlank는 String 타입에만 사용 가능
- Enum과 숫자는 "빈 문자열" 개념이 없음

### 실제 사용 예시

#### CampingItemCreateRequest (올바른 예시)

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CampingItemCreateRequest {

    @NotBlank  // String → @NotBlank
    private String name;

    @NotNull   // Enum → @NotNull
    private CampingCategory category;

    @NotBlank  // String → @NotBlank
    private String model;

    @NotBlank  // String → @NotBlank (공백으로만 이루어진 설명 방지)
    private String description;

    @NotNull   // 숫자 → @NotNull
    private Integer stockQuantity;

    @NotNull   // BigDecimal → @NotNull
    private BigDecimal baseDailyRate;

    @NotNull   // Enum → @NotNull
    private CampingItemStatus status;
}
```

### 검증 실패 시 동작

#### API 요청 예시

**잘못된 요청:**
```json
POST /api/camping-items
{
  "name": "   ",           // 공백만 있음
  "category": "TENT",
  "model": "",             // 빈 문자열
  "description": "좋은 텐트",
  "stockQuantity": null,   // null
  "baseDailyRate": 50000,
  "status": "AVAILABLE"
}
```

**응답:**
```json
HTTP 400 Bad Request
{
  "errors": [
    {
      "field": "name",
      "message": "must not be blank"
    },
    {
      "field": "model",
      "message": "must not be blank"
    },
    {
      "field": "stockQuantity",
      "message": "must not be null"
    }
  ]
}
```

### 헷갈리기 쉬운 케이스

#### Q: "4인용 텐트"는 공백이 있는데 @NotBlank로 통과되나요?

**A: 통과됩니다**

```java
@NotBlank
private String name;

// 이런 값들은 모두 통과
"4인용 텐트"         // 단어 사이 공백 OK
"구스다운 침낭"       // 단어 사이 공백 OK
"MSR 허브허브 텐트"  // 여러 공백 OK

// 이런 값들은 실패
"   "                // 공백만 있음
""                   // 빈 문자열
null                 // null
```

**@NotBlank가 체크하는 것:**
- "의미 있는 문자가 하나라도 있는가?"
- 공백만으로 이루어진 문자열인지 확인

**@NotBlank가 체크하지 않는 것:**
- 문자 사이에 공백이 있는지 (상관없음)

### 핵심 정리

#### 선택 가이드

**1. String 필드**
```java
@NotBlank private String name;          // 권장
@NotBlank private String description;   // 권장
```

**2. Enum 필드**
```java
@NotNull private CampingCategory category;     // 권장
@NotNull private CampingItemStatus status;     // 권장
```

**3. 숫자 필드**
```java
@NotNull private Integer stockQuantity;        // 권장
@NotNull private BigDecimal baseDailyRate;     // 권장
```

#### 핵심 원칙
> **"String은 @NotBlank, 나머지는 @NotNull"**

---

## DTO (Data Transfer Object) 패턴

### 기본 개념

**DTO란?**
> 계층 간 데이터를 전송하기 위한 객체. 특히 API 요청/응답 시 Entity를 직접 노출하지 않고 DTO로 변환하여 사용

### 왜 필요한가?

#### Entity를 직접 노출하면 발생하는 문제

**1. 보안 문제**
```java
// Entity를 직접 반환
@GetMapping("/api/users/{id}")
public User getUser(Long id) {
    return userRepository.findById(id);
}

// 응답에 password가 그대로 노출
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "password": "hashed_password_12345",  // 보안 위험
  "internalNote": "VIP 고객"             // 내부 정보 노출
}
```

**2. 순환 참조 문제**
```java
@Entity
public class CampingItem {
    @OneToMany
    private List<CampingRental> rentals;  // 대여 목록
}

@Entity
public class CampingRental {
    @ManyToOne
    private CampingItem campingItem;      // 장비
}

// Entity를 직접 반환하면
// CampingItem → rentals → CampingItem → rentals → ... (무한 루프)
```

**3. 불필요한 필드 노출**
```java
// 장비 목록 조회 시
{
  "id": 1,
  "name": "4인용 텐트",
  "createdAt": "2024-01-01T10:00:00",  // 목록에서는 불필요
  "updatedAt": "2024-01-05T15:30:00",  // 목록에서는 불필요
  "description": "매우 긴 설명..."      // 목록에서는 불필요
}
```

### DTO의 종류

#### Request DTO (요청용)

**1. CreateRequest - 생성 요청**
```java
public class CampingItemCreateRequest {
    // id 없음 (DB가 자동 생성)

    @NotBlank
    private String name;

    @NotNull
    private CampingCategory category;

    @NotNull
    private Integer stockQuantity;

    // createdAt, updatedAt 없음 (서버가 자동 생성)
}
```

**왜 id가 없나?**
- 생성 시점에는 id가 존재하지 않음
- DB의 `@GeneratedValue`가 자동으로 생성
- 클라이언트가 id를 지정하면 보안 위험 (다른 데이터 덮어쓰기 가능)

**왜 createdAt, updatedAt이 없나?**
- 서버에서 `LocalDateTime.now()`로 자동 설정
- 클라이언트가 보내면 시간 조작 가능 (신뢰할 수 없음)

**2. UpdateRequest - 수정 요청**
```java
public class CampingItemUpdateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String model;

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal baseDailyRate;

    // category 없음 (생성 후 카테고리 변경 불가)
    // stockQuantity 없음 (별도 API로 관리)
    // status 없음 (별도 API로 관리)
}
```

**왜 필드가 적나?**
- **수정 가능한 필드만** 포함
- category, stockQuantity, status는 별도 API로 관리 (실무 패턴)

**3. 특수 목적 Request**
```java
// 재고 증감용
public class StockUpdateRequest {
    @NotNull
    private Integer quantity;  // 증가/감소할 수량만
}

// 상태 변경용
public class StatusUpdateRequest {
    @NotNull
    private CampingItemStatus status;  // 변경할 상태만
}
```

#### Response DTO (응답용)

```java
public class CampingItemResponse {
    // id 포함 (클라이언트가 알아야 함)
    private Long id;

    private String name;
    private CampingCategory category;
    private String model;
    private String description;
    private Integer stockQuantity;
    private BigDecimal baseDailyRate;
    private CampingItemStatus status;

    // createdAt, updatedAt 포함 (조회 결과에 필요)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // validation 어노테이션 없음
    // (서버가 보내는 데이터라 검증 불필요)
}
```

### Entity ↔ DTO 변환

#### Static Factory Method 패턴

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CampingItemResponse {
    private Long id;
    private String name;
    private CampingCategory category;
    // ... 나머지 필드

    // Static Factory Method
    public static CampingItemResponse from(CampingItem entity) {
        return new CampingItemResponse(
            entity.getId(),
            entity.getName(),
            entity.getCategory(),
            entity.getModel(),
            entity.getDescription(),
            entity.getStockQuantity(),
            entity.getBaseDailyRate(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
```

**사용 예시:**
```java
// Service에서
public CampingItemResponse findById(Long id) {
    CampingItem entity = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("장비를 찾을 수 없습니다"));

    return CampingItemResponse.from(entity);  // 간단한 변환
}
```

**장점:**
- 변환 로직이 DTO에 응집
- `new CampingItemResponse(...)`보다 의미 명확
- 유지보수 용이

### Request DTO → Entity 변환

#### Builder 패턴 활용

```java
// Service에서
public CampingItemResponse create(CampingItemCreateRequest request) {
    // DTO → Entity 변환
    CampingItem entity = CampingItem.builder()
        .name(request.getName())
        .category(request.getCategory())
        .model(request.getModel())
        .description(request.getDescription())
        .stockQuantity(request.getStockQuantity())
        .baseDailyRate(request.getBaseDailyRate())
        .status(request.getStatus())
        .build();  // createdAt, updatedAt은 생성자에서 자동 설정

    CampingItem saved = repository.save(entity);

    // Entity → Response DTO 변환
    return CampingItemResponse.from(saved);
}
```

### 실무 활용 예시

#### API 전체 흐름

```java
// 1. 클라이언트 요청
POST /api/camping-items
{
  "name": "4인용 텐트",
  "category": "TENT",
  "model": "MSR 허브허브",
  "description": "4인용 돔 텐트",
  "stockQuantity": 10,
  "baseDailyRate": 50000,
  "status": "AVAILABLE"
}

// 2. Controller - Request DTO로 받음
@PostMapping("/api/camping-items")
public ResponseEntity<CampingItemResponse> create(
    @Valid @RequestBody CampingItemCreateRequest request) {  // DTO로 받음

    CampingItemResponse response = campingItemService.create(request);
    return ResponseEntity.ok(response);
}

// 3. Service - DTO → Entity 변환, 처리 후 Entity → DTO 변환
public CampingItemResponse create(CampingItemCreateRequest request) {
    CampingItem entity = CampingItem.builder()
        .name(request.getName())
        // ...
        .build();

    CampingItem saved = repository.save(entity);
    return CampingItemResponse.from(saved);  // DTO로 변환
}

// 4. 클라이언트 응답 - Response DTO
{
  "id": 1,                              // 생성된 id
  "name": "4인용 텐트",
  "category": "TENT",
  "model": "MSR 허브허브",
  "description": "4인용 돔 텐트",
  "stockQuantity": 10,
  "baseDailyRate": 50000,
  "status": "AVAILABLE",
  "createdAt": "2024-12-31T17:00:00",   // 서버가 자동 생성
  "updatedAt": "2024-12-31T17:00:00"    // 서버가 자동 생성
}
```

### DTO 설계 원칙

#### Request DTO

**CreateRequest:**
- id 제외 (DB 자동 생성)
- createdAt, updatedAt 제외 (서버 자동 생성)
- validation 포함 (@NotNull, @NotBlank)
- 생성에 필요한 모든 필드

**UpdateRequest:**
- 수정 가능한 필드만
- validation 포함
- 변경 불가 필드 제외 (category 등)
- 별도 API로 관리할 필드 제외 (stockQuantity, status)

**특수 목적 Request:**
- 해당 작업에 필요한 최소 필드만
- 예: StockUpdateRequest는 quantity만

#### Response DTO

- 모든 정보 포함 (id, createdAt, updatedAt 포함)
- validation 불필요 (서버가 보내는 데이터)
- static factory method (from()) 제공
- Entity의 민감한 정보는 제외 가능

### 비교표

| 항목 | Entity | CreateRequest | UpdateRequest | Response |
|------|--------|---------------|---------------|----------|
| id | O (자동 생성) | X | X | O |
| 비즈니스 필드 | O | O | O (수정 가능한 것만) | O |
| createdAt | O (자동 생성) | X | X | O |
| updatedAt | O (자동 설정) | X | X | O |
| validation | X | O | O | X |
| 비즈니스 로직 | O | X | X | X |

### 핵심 정리

#### DTO 사용 이유
1. **보안**: 민감한 정보 노출 방지
2. **순환 참조 방지**: Entity 간 관계로 인한 무한 루프 방지
3. **API 스펙 명확화**: 필요한 데이터만 주고받음
4. **유연성**: API 변경이 Entity에 영향 없음

#### 변환 패턴
- **Request → Entity**: Builder 패턴
- **Entity → Response**: Static Factory Method (`from()`)

#### 핵심 원칙
> **"Entity는 절대 API에 직접 노출하지 않는다. 항상 DTO로 변환한다"**

---

## Static Factory Method 패턴

### 기본 개념

**Static Factory Method란?**
> 객체를 생성하는 정적(static) 메서드. 생성자 대신 메서드를 통해 객체를 생성하는 패턴

일반적인 생성자 호출 대신 `ClassName.methodName()` 형태로 객체를 생성한다.

### 일반 생성자 vs Static Factory Method

#### 일반 생성자 사용

```java
// DTO 클래스
@Getter
@AllArgsConstructor
public class CampingItemResponse {
    private Long id;
    private String name;
    private CampingCategory category;
    private String model;
    private String description;
    private Integer stockQuantity;
    private BigDecimal baseDailyRate;
    private CampingItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Service에서 사용
public CampingItemResponse findById(Long id) {
    CampingItem entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("장비를 찾을 수 없습니다"));

    // 생성자 직접 호출
    return new CampingItemResponse(
        entity.getId(),
        entity.getName(),
        entity.getCategory(),
        entity.getModel(),
        entity.getDescription(),
        entity.getStockQuantity(),
        entity.getBaseDailyRate(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
}
```

**문제점:**
- 필드가 많으면 코드가 길어짐
- 어떤 의미로 변환하는지 불명확
- Entity → DTO 변환 로직이 Service에 흩어짐
- 필드 순서를 잘못 입력할 위험

#### Static Factory Method 사용

```java
// DTO 클래스
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CampingItemResponse {
    private Long id;
    private String name;
    private CampingCategory category;
    private String model;
    private String description;
    private Integer stockQuantity;
    private BigDecimal baseDailyRate;
    private CampingItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static Factory Method
    public static CampingItemResponse from(CampingItem entity) {
        return new CampingItemResponse(
            entity.getId(),
            entity.getName(),
            entity.getCategory(),
            entity.getModel(),
            entity.getDescription(),
            entity.getStockQuantity(),
            entity.getBaseDailyRate(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

// Service에서 사용
public CampingItemResponse findById(Long id) {
    CampingItem entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("장비를 찾을 수 없습니다"));

    // Static Factory Method 호출
    return CampingItemResponse.from(entity);
}
```

### Static Factory Method의 장점

#### 1. 가독성 향상

```java
// 생성자 직접 호출
return new CampingItemResponse(entity.getId(), entity.getName(), ...);

// Static Factory Method
return CampingItemResponse.from(entity);  // "entity로부터 Response를 만든다"는 의미 명확
```

메서드 이름이 의미를 전달한다. `from()`, `of()`, `valueOf()` 등의 관습적인 이름을 사용한다.

#### 2. 변환 로직 캡슐화

```java
// 변환 로직이 DTO 안에 모임
public class CampingItemResponse {
    public static CampingItemResponse from(CampingItem entity) {
        // 변환 로직이 한 곳에 집중
        return new CampingItemResponse(...);
    }
}
```

Entity → DTO 변환 로직이 DTO 클래스에 응집된다.

Service에서는 단순히 `from(entity)`만 호출하면 된다.

#### 3. 유지보수 용이

```java
// 필드 추가 시
public class CampingItemResponse {
    private Long id;
    private String name;
    private String imageUrl;  // 새 필드 추가

    public static CampingItemResponse from(CampingItem entity) {
        return new CampingItemResponse(
            entity.getId(),
            entity.getName(),
            entity.getImageUrl()  // 한 곳만 수정
        );
    }
}
```

필드가 추가되어도 Static Factory Method 한 곳만 수정하면 된다.

생성자 직접 호출했다면 Service의 모든 코드를 수정해야 한다.

#### 4. 타입 안정성

```java
// 생성자: 파라미터 순서 잘못 입력 가능
new CampingItemResponse(
    entity.getName(),        // 잘못된 순서
    entity.getId(),
    ...
);  // 컴파일 에러는 안 나지만 잘못된 데이터

// Static Factory Method: 메서드 내부에서 올바른 순서 보장
CampingItemResponse.from(entity);  // 안전
```

### 네이밍 관습

일반적으로 사용되는 Static Factory Method 이름:

**1. from**
```java
// 하나의 파라미터를 받아 인스턴스 생성
public static CampingItemResponse from(CampingItem entity) { ... }
```

**2. of**
```java
// 여러 파라미터를 받아 인스턴스 생성
public static LocalDate of(int year, int month, int day) { ... }
```

**3. valueOf**
```java
// from/of와 유사하지만 더 장황한 버전
public static Integer valueOf(String s) { ... }
```

**4. getInstance / newInstance**
```java
// 싱글톤 패턴에서 주로 사용
public static Singleton getInstance() { ... }
```

### 실무 활용 예시

#### Service 계층에서 일관된 사용

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CampingItemService {
    private final CampingItemRepository repository;

    public CampingItemResponse findById(Long id) {
        CampingItem entity = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("장비를 찾을 수 없습니다"));

        return CampingItemResponse.from(entity);
    }

    public List<CampingItemResponse> findAll() {
        return repository.findAll().stream()
            .map(CampingItemResponse::from)  // 메서드 참조로 간결하게
            .toList();
    }

    @Transactional
    public CampingItemResponse create(CampingItemCreateRequest request) {
        CampingItem entity = CampingItem.builder()
            .name(request.getName())
            // ...
            .build();

        CampingItem saved = repository.save(entity);
        return CampingItemResponse.from(saved);
    }

    @Transactional
    public CampingItemResponse update(Long id, CampingItemUpdateRequest request) {
        CampingItem entity = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("장비를 찾을 수 없습니다"));

        entity.updateInfo(request.getName(), request.getModel(),
                         request.getDescription(), request.getBaseDailyRate());

        return CampingItemResponse.from(entity);
    }
}
```

모든 메서드에서 `CampingItemResponse.from(entity)`로 일관되게 변환한다.

#### Stream API와 조합

```java
// 메서드 참조로 간결하게 변환
public List<CampingItemResponse> findAll() {
    return repository.findAll().stream()
        .map(CampingItemResponse::from)  // entity -> CampingItemResponse.from(entity)
        .toList();
}

// 람다 표현식으로 작성 시
public List<CampingItemResponse> findAll() {
    return repository.findAll().stream()
        .map(entity -> CampingItemResponse.from(entity))
        .toList();
}
```

`::from`은 메서드 참조로, `entity -> CampingItemResponse.from(entity)`와 동일하다.

### Builder 패턴과의 비교

#### Builder 패턴 (Request → Entity)

```java
// Request → Entity 변환
CampingItem entity = CampingItem.builder()
    .name(request.getName())
    .category(request.getCategory())
    .model(request.getModel())
    .description(request.getDescription())
    .stockQuantity(request.getStockQuantity())
    .baseDailyRate(request.getBaseDailyRate())
    .status(request.getStatus())
    .build();
```

Builder는 여러 필드를 선택적으로 설정할 때 유용하다.

#### Static Factory Method (Entity → Response)

```java
// Entity → Response 변환
CampingItemResponse response = CampingItemResponse.from(entity);
```

Static Factory Method는 변환 로직을 캡슐화할 때 유용하다.

### 주의사항

#### NoArgsConstructor와 AllArgsConstructor 필요

```java
@Getter
@NoArgsConstructor  // 필수: JPA, Jackson 등에서 사용
@AllArgsConstructor // 필수: from() 메서드에서 사용
public class CampingItemResponse {
    private Long id;
    private String name;
    // ...

    public static CampingItemResponse from(CampingItem entity) {
        return new CampingItemResponse(  // AllArgsConstructor 사용
            entity.getId(),
            entity.getName(),
            // ...
        );
    }
}
```

`@AllArgsConstructor`가 없으면 Static Factory Method에서 생성자를 호출할 수 없다.

`@NoArgsConstructor`는 JPA, Jackson(JSON 변환) 등에서 기본 생성자가 필요하기 때문에 함께 사용한다.

### 핵심 정리

#### Static Factory Method 정의
객체 생성을 담당하는 정적 메서드로, 생성자 대신 의미 있는 이름으로 객체를 생성한다.

#### 사용 목적
- Entity → DTO 변환 로직 캡슐화
- 가독성 향상 (`from()`, `of()` 등 의미 있는 이름)
- 유지보수성 향상 (변환 로직이 한 곳에 집중)

#### 실무 패턴
- **Request → Entity**: Builder 패턴
- **Entity → Response**: Static Factory Method

#### 핵심 원칙
> **"객체 변환 로직은 대상 클래스 안에 정적 메서드로 캡슐화한다"**

---

## Controller 단위 테스트 (@WebMvcTest)

### 기본 개념

**Controller 단위 테스트란?**
> Controller 계층만 독립적으로 테스트하는 방식. Service 계층은 Mock으로 대체하여 Controller의 역할만 검증한다.

### 핵심 질문: "Service가 가짜인데 테스트 의미가 있어?"

**답: 있다**

Controller 테스트는 **비즈니스 로직을 테스트하는 게 아니다.**

#### Controller 테스트가 검증하는 것

**1. HTTP 라우팅**
```java
// 잘못된 경로 설정 감지
@PostMapping("/create")  // 오타
public ResponseEntity<CampingItemResponse> create(...) { ... }

// 테스트 실패: 404 Not Found
// 올바른 경로: POST /api/camping-items
```

**2. 요청 파싱 (JSON → DTO)**
```java
// JSON 요청
{
  "name": "4인용 텐트",
  "category": "TENT",
  "stockQuantity": 10
}

// Controller가 CampingItemCreateRequest로 제대로 변환하는가?
```

**3. Validation 동작**
```java
@PostMapping("/api/camping-items")
public ResponseEntity<CampingItemResponse> create(
    @Valid @RequestBody CampingItemCreateRequest request) {  // @Valid 체크
    // ...
}

// @Valid를 빼먹으면?
// 잘못된 데이터도 통과 (테스트로 감지)
```

**4. 응답 생성 (DTO → JSON)**
```java
// Response 객체가 JSON으로 올바르게 변환되는가?
// 상태코드가 201 Created인가? 200 OK가 아니라?
```

**5. 에러 처리**
```java
// name이 공백이면 400 Bad Request를 반환하는가?
// Validation 실패 시 적절한 에러 메시지를 주는가?
```

### @WebMvcTest vs @SpringBootTest

#### 비교표

| 항목 | @WebMvcTest | @SpringBootTest |
|------|-------------|-----------------|
| 로드 범위 | Controller만 | 전체 애플리케이션 |
| Service | Mock으로 대체 | 실제 Service |
| DB | 연결 안 함 | 실제 연결 |
| 속도 | 빠름 (1~2초) | 느림 (5~10초) |
| 목적 | Controller 단위 테스트 | 통합 테스트 |
| 사용 시기 | Controller 로직 검증 | 전체 흐름 검증 |

#### @WebMvcTest (단위 테스트)

```java
@WebMvcTest(CampingItemController.class)  // Controller만 로드
@AutoConfigureMockMvc(addFilters = false)  // Security 비활성화
class CampingItemControllerTest {

    @Autowired
    private MockMvc mockMvc;  // HTTP 요청/응답 시뮬레이션

    @MockitoBean  // Service를 Mock으로 대체
    private CampingItemService campingItemService;

    // Controller만 테스트
}
```

**특징:**
- Controller만 로드 (빠름)
- Service는 가짜 객체 (Mock)
- DB 연결 없음
- HTTP 요청/응답만 검증

**장점:**
- 빠른 실행 속도
- Controller 로직만 집중 테스트
- 외부 의존성 없음

**단점:**
- Service 로직은 테스트 안 됨
- DB 연동은 테스트 안 됨

#### @SpringBootTest (통합 테스트)

```java
@SpringBootTest  // 전체 애플리케이션 로드
@AutoConfigureMockMvc
class CampingItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // 실제 Service, Repository, DB 사용
    // Service는 Mock이 아님
}
```

**특징:**
- 전체 애플리케이션 로드 (느림)
- 실제 Service, Repository 사용
- DB 실제 연결
- 전체 흐름 검증

**장점:**
- 실제 동작 검증
- 통합 시나리오 테스트

**단점:**
- 느린 실행 속도
- 테스트 격리 어려움

### Given-When-Then 패턴

테스트 코드를 구조화하는 표준 패턴

#### 구조

```java
@Test
@DisplayName("캠핑 장비 생성 성공")
void create_Success() throws Exception {
    // Given: 테스트 데이터 준비
    CampingItemCreateRequest request = new CampingItemCreateRequest(
        "4인용 텐트", CampingCategory.TENT, "MSR", "가족용",
        10, new BigDecimal("50000"), CampingItemStatus.AVAILABLE
    );

    CampingItemResponse response = new CampingItemResponse(
        1L, "4인용 텐트", CampingCategory.TENT, "MSR", "가족용",
        10, new BigDecimal("50000"), CampingItemStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now()
    );

    // Mock 설정: Service 호출 시 response 반환
    given(campingItemService.create(any(CampingItemCreateRequest.class)))
        .willReturn(response);

    // When: 실제 동작 실행
    mockMvc.perform(post("/api/camping-items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        // Then: 결과 검증
        .andExpect(status().isCreated())  // 201 상태코드
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("4인용 텐트"))
        .andExpect(jsonPath("$.category").value("TENT"))
        .andExpect(jsonPath("$.stockQuantity").value(10));
}
```

#### 각 단계의 역할

**Given (준비)**
- 테스트에 필요한 데이터 생성
- Mock 객체 동작 정의
- `given().willReturn()` 사용

**When (실행)**
- 실제 테스트할 동작 실행
- `mockMvc.perform()` 사용
- HTTP 요청 시뮬레이션

**Then (검증)**
- 결과가 예상과 일치하는지 검증
- `.andExpect()` 사용
- 상태코드, 응답 데이터 검증

### MockMvc 주요 메서드

#### 요청 생성

```java
// POST 요청
mockMvc.perform(post("/api/camping-items")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(request)))

// GET 요청
mockMvc.perform(get("/api/camping-items/{id}", 1L))

// PUT 요청
mockMvc.perform(put("/api/camping-items/{id}", 1L)
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(updateRequest)))
```

#### 응답 검증

```java
// 상태코드 검증
.andExpect(status().isOk())          // 200
.andExpect(status().isCreated())     // 201
.andExpect(status().isBadRequest())  // 400
.andExpect(status().isNotFound())    // 404

// JSON 경로로 검증
.andExpect(jsonPath("$.id").value(1))
.andExpect(jsonPath("$.name").value("4인용 텐트"))
.andExpect(jsonPath("$.category").value("TENT"))

// 배열 검증
.andExpect(jsonPath("$.length()").value(2))  // 배열 크기
.andExpect(jsonPath("$[0].name").value("텐트"))  // 첫 번째 요소
.andExpect(jsonPath("$[1].name").value("침낭"))  // 두 번째 요소
```

### @MockitoBean (Spring Boot 3.4+)

#### 변경 사항

```java
// 구버전 (deprecated)
import org.springframework.boot.test.mock.mockito.MockBean;
@MockBean
private CampingItemService campingItemService;

// 신버전 (권장)
import org.springframework.test.context.bean.override.mockito.MockitoBean;
@MockitoBean
private CampingItemService campingItemService;
```

**차이점:**
- 기능은 완전히 동일
- 패키지 경로만 변경됨
- Spring Boot 3.4부터 새로운 API 도입

#### Mock 동작 정의

```java
// any(): 아무 값이나 매칭
given(campingItemService.create(any(CampingItemCreateRequest.class)))
    .willReturn(response);

// eq(): 특정 값과 정확히 매칭
given(campingItemService.findById(eq(1L)))
    .willReturn(response);

// anyLong(), anyString() 등도 사용 가능
given(campingItemService.findById(anyLong()))
    .willReturn(response);
```

### 실제 테스트 예시

#### 성공 케이스 테스트

```java
@Test
@DisplayName("캠핑 장비 생성 성공")
void create_Success() throws Exception {
    // Given
    CampingItemCreateRequest request = new CampingItemCreateRequest(
        "4인용 텐트", CampingCategory.TENT, "MSR", "가족용 텐트",
        10, new BigDecimal("50000"), CampingItemStatus.AVAILABLE
    );

    CampingItemResponse response = new CampingItemResponse(
        1L, "4인용 텐트", CampingCategory.TENT, "MSR", "가족용 텐트",
        10, new BigDecimal("50000"), CampingItemStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now()
    );

    given(campingItemService.create(any(CampingItemCreateRequest.class)))
        .willReturn(response);

    // When & Then
    mockMvc.perform(post("/api/camping-items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("4인용 텐트"));
}
```

#### Validation 실패 테스트

```java
@Test
@DisplayName("이름이 공백이면 생성 실패")
void create_ValidationFail_WhenNameIsBlank() throws Exception {
    // Given: 잘못된 요청 (name이 공백)
    CampingItemCreateRequest request = new CampingItemCreateRequest(
        "   ",  // 공백만
        CampingCategory.TENT, "MSR", "가족용 텐트",
        10, new BigDecimal("50000"), CampingItemStatus.AVAILABLE
    );

    // When & Then
    mockMvc.perform(post("/api/camping-items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());  // 400 에러
}
```

#### 목록 조회 테스트

```java
@Test
@DisplayName("캠핑 장비 전체 조회 성공")
void findAll_Success() throws Exception {
    // Given
    CampingItemResponse response1 = new CampingItemResponse(
        1L, "4인용 텐트", CampingCategory.TENT, "MSR", "가족용",
        10, new BigDecimal("50000"), CampingItemStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now()
    );

    CampingItemResponse response2 = new CampingItemResponse(
        2L, "침낭", CampingCategory.SLEEPING_BAG, "Model-B", "겨울용",
        5, new BigDecimal("30000"), CampingItemStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now()
    );

    given(campingItemService.findAll())
        .willReturn(List.of(response1, response2));

    // When & Then
    mockMvc.perform(get("/api/camping-items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("4인용 텐트"))
        .andExpect(jsonPath("$[1].name").value("침낭"));
}
```

### 테스트 설정

#### build.gradle

```gradle
tasks.named('test') {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
    }
}
```

**효과:**
- 터미널에서 테스트 결과 실시간 확인
- PASSED/FAILED 상태 출력

#### Security 비활성화

```java
@WebMvcTest(CampingItemController.class)
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 비활성화
class CampingItemControllerTest {
    // Spring Security가 있어도 인증 없이 테스트 가능
}
```

**이유:**
- Controller 테스트는 인증/인가 검증이 목적이 아님
- Security 테스트는 별도로 작성
- 단순한 Controller 로직만 테스트

### 트러블슈팅

#### 문제 1: Spring Boot 4.0.0 테스트 의존성 문제

**증상:**
```
error: package org.springframework.boot.test.mock.mockito does not exist
error: package com.fasterxml.jackson.databind does not exist
```

**원인:**
- Spring Boot 4.0.0은 불안정 (테스트 인프라 변경)
- Jackson 3.0으로 업그레이드 (패키지 경로 변경)

**해결:**
```gradle
// build.gradle
plugins {
    id 'org.springframework.boot' version '3.4.1'  // 4.0.0 → 3.4.1
}
```

#### 문제 2: 403 Forbidden 에러

**증상:**
```
Status expected:<201> but was:<403>
```

**원인:**
- Spring Security가 활성화되어 모든 요청 인증 요구

**해결:**
```java
@WebMvcTest(CampingItemController.class)
@AutoConfigureMockMvc(addFilters = false)  // 추가
class CampingItemControllerTest { ... }
```

#### 문제 3: @MockBean deprecated 경고

**증상:**
```
warning: [removal] MockBean has been deprecated and marked for removal
```

**해결:**
```java
// 변경 전
import org.springframework.boot.test.mock.mockito.MockBean;
@MockBean

// 변경 후
import org.springframework.test.context.bean.override.mockito.MockitoBean;
@MockitoBean
```

### 테스트 전략

#### 무엇을 테스트할까?

**Controller 테스트로 검증:**
- HTTP 라우팅 (`/api/camping-items`)
- 요청/응답 직렬화 (JSON ↔ DTO)
- Validation (`@Valid`)
- 상태코드 (201, 200, 400 등)
- 에러 처리

**Service 테스트로 검증:**
- 비즈니스 로직
- 데이터 변환
- 예외 처리
- 트랜잭션

**통합 테스트로 검증:**
- Controller → Service → Repository 전체 흐름
- DB 연동
- 실제 시나리오

### 핵심 정리

#### Controller 단위 테스트의 목적
> **"Controller는 HTTP 요청을 받아 Service에 전달하고, 결과를 HTTP 응답으로 변환하는 역할을 제대로 하는가?"**

#### 테스트 계층별 역할

| 계층 | 테스트 대상 | Mock 사용 | 속도 |
|------|------------|----------|------|
| Controller 테스트 | 라우팅, 직렬화, Validation | Service Mock | 빠름 |
| Service 테스트 | 비즈니스 로직 | Repository Mock | 빠름 |
| 통합 테스트 | 전체 흐름 | Mock 없음 | 느림 |

#### 핵심 원칙
> **"각 계층은 자신의 책임만 테스트한다. Controller는 비즈니스 로직을 테스트하지 않는다."**