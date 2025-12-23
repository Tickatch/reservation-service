# Tickatch Reservation Service


티켓 예매 플랫폼 Tickatch의 예매 관리 마이크로서비스입니다.

## 프로젝트 소개

---
Reservation Service는 예매 전 과정의 상태 흐름을 관리하며, 결제 결과와의 동기화를 통해 안정적인 티켓 예약 처리를 담당합니다.

## 기술 스택

---

| 분류            | 기술                       |
|---------------|--------------------------|
| Framework     | Spring Boot 3.x          |
| Language      | Java 21                  |
| Database      | PostgreSQL               |
| Messaging     | RabbitMQ                 |
| Query         | QueryDSL / JPA           |
| Communication | OpenFeign / RestTemplate |
| Security      | Spring Security          |

## 아키텍처

---

### 시스템 구성

```
┌────────────────────────────────────────────────────────────┐
│                          Tickatch Platform                 │
├──────────────┬──────────────┬──────────────┬───────────────┤
│     User     │     Auth     │   Product    │    ArtHall    │
│   Service    │   Service    │   Service    │    Service    │
├──────────────┼──────────────┼──────────────┼───────────────┤
│ Reservation  │    Ticket    │ Reservation  │    Payment    │
│   Service    │   Service    │     Seat     │    Service    │
│              │              │   Service    │               │
├──────────────┴──────────────┴──────────────┴───────────────┤
│    Notification Service     │         Log Service          │
└────────┬────────────────────┴──────────┬───────────────────┘
         │                               │
         └────────────────────┬──────────┘
                              │
                          RabbitMQ
```

### 레이어 구조

```
src/main/java/com/tickatch/reservationservice
├── ReservationServiceApplication.java
├── global
│   ├── config
│   │   ├── AuthExtractor.java
│   │   ├── FeignConfig.java
│   │   ├── FeignErrorDecoder.java
│   │   ├── FeignRequestInterceptor.java
│   │   └── SecurityConfig.java
│   ├── domain
│   │   ├── AbstractAuditEntity.java
│   │   └── AbstractTimeEntity.java
│   └── security
│       └── ActorExtractor.java
└── reservation
    ├── application
    │   ├── dto
    │   │   ├── request
    │   │   │   ├── CancelRequest.java
    │   │   │   ├── PendingPaymentRequest.java
    │   │   │   └── ReservationRequest.java
    │   │   └── response
    │   │       ├── ReservationDetailResponse.java
    │   │       └── ReservationResponse.java
    │   ├── event
    │   │   ├── ReservationCanceledEvent.java
    │   │   ├── ReservationCompletedEvent.java
    │   │   ├── ReservationCompletedEventListener.java
    │   │   └── ReservationRefundEventListener.java
    │   ├── helper
    │   │   ├── PaymentResultApplyHelper.java
    │   │   ├── ProductCancelHelper.java
    │   │   ├── ReservationCancelHelper.java
    │   │   └── ReservationExpireHelper.java
    │   ├── messaging
    │   │   └── event
    │   │       └── ProductCancelledEvent.java
    │   ├── port
    │   │   ├── ReservationEventPublisherPort.java
    │   │   └── ReservationLogPort.java
    │   ├── scheduler
    │   │   └── ReservationExpireScheduler.java
    │   └── service
    │       └── ReservationService.java
    ├── domain
    │   ├── ProductInfo.java
    │   ├── Reservation.java
    │   ├── ReservationId.java
    │   ├── ReservationStatus.java
    │   ├── Reserver.java
    │   ├── dto
    │   │   ├── ProductInformation.java
    │   │   └── UserInformation.java
    │   ├── event
    │   │   └── ReservationCompletedDomainEvent.java
    │   ├── exception
    │   │   ├── ReservationErrorCode.java
    │   │   └── ReservationException.java
    │   ├── repository
    │   │   ├── ReservationDetailsRepository.java
    │   │   └── ReservationRepository.java
    │   └── service
    │       ├── PaymentService.java
    │       ├── ProductService.java
    │       ├── SeatPreemptService.java
    │       ├── TicketService.java
    │       └── UserService.java
    ├── infrastructure
    │   ├── api
    │   │   ├── PaymentServiceImpl.java
    │   │   ├── ProductServiceImpl.java
    │   │   ├── SeatPreemptServiceImpl.java
    │   │   ├── TicketServiceImpl.java
    │   │   └── UserServiceImpl.java
    │   ├── client
    │   │   ├── PaymentFeignClient.java
    │   │   ├── ProductFeignClient.java
    │   │   ├── SeatFeignClient.java
    │   │   ├── TicketFeignClient.java
    │   │   ├── UserFeignClient.java
    │   │   └── dto
    │   │       ├── PaymentRefundRequest.java
    │   │       ├── ProductClientResponse.java
    │   │       └── UserClientResponse.java
    │   ├── exception
    │   │   ├── SeatApiErrorCode.java
    │   │   ├── SeatApiException.java
    │   │   ├── TicketApiErrorCode.java
    │   │   └── TicketApiException.java
    │   ├── messaging
    │   │   ├── config
    │   │   │   └── RabbitMQConfig.java
    │   │   ├── consumer
    │   │   │   └── ProductCancelledEventConsumer.java
    │   │   ├── event
    │   │   │   └── ReservationLogEvent.java
    │   │   └── publisher
    │   │       ├── ReservationEventPublisher.java
    │   │       └── ReservationLogPublisher.java
    │   └── persistence
    │       ├── ReservationDao.java
    │       └── config
    │           └── JPAConfig.java
    └── presentation
        ├── api
        │   └── ReservationApi.java
        └── dto
            ├── CreateReservationRequest.java
            ├── PaymentResultRequest.java
            └── ReservationCancelRequest.java
```

## 도메인 모델

---

### Reservation (Aggregate Root)

예매의 전체 라이프사이클을 관리하는 핵심 엔티티입니다.

```
Reservation
├── 기본 정보
│   ├── reservationId          # 예매 ID
│   ├── reservationNumber      # 예매 번호
│   └── expireAt               # 만료 시각
│
├── 예매자 정보
│   └── Reserver
│
├── 상품 정보
│   └── ProductInfo 
│
└── 상태
    └── ReservationStatus
```

### Value Objects

| VO                | 설명    | 주요 필드                                                               |
|-------------------|-------|---------------------------------------------------------------------|
| ReservationStatus | 예매 상태 | INIT, PENDING_PAYMENT, CONFIRMED, PAYMENT_FAILED, CANCELED, EXPIRED |

### Reserver

예매자 관련 정보를 관리하는 엔티티입니다. Reservation에 종속됩니다.

| 필드   | 설명     |
|------|--------|
| id   | 예매자 id |
| name | 예매자명   |

### ProductInfo

예매 상품과 관련된 정보를 관리하는 엔티티입니다. Reservation에 종속됩니다.

| 필드          | 설명    |
|-------------|-------|
| productId   | 상품 id |
| productName | 상품명   |
| seatId      | 좌석 id |
| seatNumber  | 좌석번호  |
| price       | 가격    |

## 예매 상태(ReservationStatus)

---

### 상태 종류

| 상태                | 설명                   | 최종 상태 |
|-------------------|----------------------|:-----:|
| `INIT`            | 예매 생성 - 초기 상태        |   ❌   |
| `PENDING_PAYMENT` | 결제 진행중 - 결제 대기       |   ❌   |
| `CONFIRMED`       | 결제 승인 - 예매 확정, 좌석 확정 |   ✅   |
| `PAYMENT_FAILED`  | 결제 실패                |   ❌   |
| `CANCELED`        | 사용자 취소               |   ✅   |
| `EXPIRED`         | 예매 시간 만료로 인한 취소      |   ✅   |

### 상태 전이 다이어그램

```
┌──────────┐
│ EXPIRED  │ (최종)
└──────────┘
    ↑
    │ (10분 초과)
    │
┌──────┐     ┌─────────────────┐     ┌─────────────┐
│ INIT │────→│ PENDING_PAYMENT │────→│  CONFIRMED  │ (최종)
└──────┘     └─────────────────┘     └─────────────┘
                      │
                      │ (결제 실패)
                      ↓
              ┌────────────────┐     ┌───────────┐
              │ PAYMENT_FAILED │────→│ CANCELED  │ (최종)
              └────────────────┘     └───────────┘
```

### 자동 전이 (스케줄러)

| 전이             | 조건          | 실행 주기 |
|----------------|-------------|-------|
| INIT → EXPIRED | 생성 후 10분 초과 | 매 분   |

## 주요 기능

---

### 예매 관리

- 예매 생성(INIT 상태로 시작)
    - 예매 생성 시 좌석 선점
- 예매 조회
    - 예매 단건 조회
    - 사용자별 예매 목록 조회
- 예매 취소
    - 예매 확정 전 취소 : 예매 취소 처리 및 좌석 선점 취소
    - 예매 확정 후 취소 : 결제 환불을 동반한 취소 처리 및 좌석 선점/티켓 취소

### 예매 상태 관리

- 상태 관리
    - 예매 상태 전이 관리 : PENDING → CONFIRMED → CANCELED / EXPIRED
    - 결제 결과에 따른 상태 동기화
- 예매 만료 처리
    - 결제 미진행 예매에 대한 자동 만료
    - 만료 시 좌석 선점 취소

## API 명세

---

Base URL: `/api/v1/reservations`

### 조회

| Method | Endpoint             | 설명            | 인증 |
|--------|----------------------|---------------|:--:|
| GET    | `/{id}`              | 예매 상세 조회      | ✅  |
| GET    | `/{reserverId}/list` | 예매자별 예매 목록 조회 | ✅  |
| GET    | `/{id}/confirmed`    | 예매 확정 상태 조회   | ❌  |

### 생성/관리

| Method | Endpoint | 설명    | 인증 |
|--------|----------|-------|:--:|
| POST   | `/`      | 예매 생성 | ❌  |

### 상태 변경

| Method | Endpoint           | 설명                                | 인증 |
|--------|--------------------|-----------------------------------|:--:|
| PATCH  | `/payment-result`  | 결제 결과 수신 (결제 서비스 → 예매 서비스)        | ❌  |
| PATCH  | `/pending-payment` | 예매 상태 변경 (INIT → PENDING_PAYMENT) | ❌  |
| POST   | `/cancel`          | 예매 리스트 취소 (CANCELED 처리)           | ✅  |

### Request DTOs

#### CreateReservationRequest (예매 생성)

| 구분         | 필드           | 타입     | 필수 | 설명                 |
|------------|--------------|--------|:--:|--------------------|
| **예매자 정보** | reserverId   | UUID   | ✅  | 예매자 ID             |
|            | reserverName | String | ✅  | 예매자 이름             |
| **상품 정보**  | productId    | Long   | ✅  | 상품 ID              |
|            | productName  | String | ✅  | 상품명                |
| **좌석 정보**  | seatId       | Long   | ✅  | 좌석 ID              |
|            | seatNumber   | String | ✅  | 좌석 번호 (예: A1, B12) |
| **결제 정보**  | price        | Long   | ✅  | 가격 (원)             |

#### ReservationCancelRequest (예매 취소)

| 구분        | 필드             | 타입           | 필수 | 설명                       |
|-----------|----------------|--------------|:--:|--------------------------|
| **취소 정보** | reservationIds | List\<UUID\> | ✅  | 취소할 예매 ID 리스트 (다건 취소 지원) |

#### PaymentResultRequest (결제 결과 수신)

| 구분        | 필드             | 타입             | 필수 | 설명                           |
|-----------|----------------|----------------|:--:|------------------------------|
| **결제 정보** | status         | String         | ✅  | 결제 결과 상태 (SUCCESS, FAILED 등) |
| **예매 정보** | reservationIds | List\<String\> | ✅  | 결제 처리된 예매 ID 리스트             |

## 이벤트

--- 

### 발행 이벤트 (Producer)

| 이벤트                       | Routing Key                          | 대상 서비스               | 설명            |
|---------------------------|--------------------------------------|----------------------|---------------|
| ReservationCompletedEvent | `reservation.completed.notification` | Notification Service | 예매 확정 시 알림 발송 |

### 수신 이벤트 (Consumer)

| 이벤트                                | Queue                                          | 처리 내용    |
|------------------------------------|------------------------------------------------|----------|
| ProductCancelledToReservationEvent | `tickatch.product.cancelled.reservation.queue` | 예매 취소 처리 |

## 외부 연동

---

### Feign Client

| 서비스           | 용도                    |
|---------------|-----------------------|
| PaymentClient | 환불 대상 예매 정보 전달        |
| ProductClient | 상품 관련 정보 수집           |
| SeatClient    | 선점/선점 취소/예매할 좌석 정보 전달 |
| TicketClient  | 티켓 취소가 필요한 예매 정보 전달   |
| UserClient    | 예매자 정보 수집             |

## 실행 방법

---

### 환경 변수

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tickatch
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  rabbitmq:
    host: localhost
    port: 5672
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
```

### 실행

```bash
./gradlew bootRun
```

### 테스트

```bash
./gradlew test
```

### 코드 품질 검사

```bash
./gradlew spotlessApply spotbugsMain spotbugsTest
```

## 데이터 모델

### ERD

```
┌─────────────────────────────────────────────────────────────────┐
│                        p_reservation                            │
├─────────────────────────────────────────────────────────────────┤
│ id                    UUID PK                                   │
├─────────────────────────────────────────────────────────────────┤
│ -- 예매 정보 --                                                   │
│ reservation_number    VARCHAR(255) NOT NULL UNIQUE              │
│ status                VARCHAR NOT NULL                          │
│                       (INIT/PENDING_PAYMENT/CONFIRMED/          │
│                        PAYMENT_FAILED/CANCELED/EXPIRED)         │
│ expire_at             TIMESTAMP NOT NULL                        │
├─────────────────────────────────────────────────────────────────┤
│ -- 예매자 정보 --                                                  │
│ reserver_id           UUID  NOT NULL                            │
│ reserver_name         VARCHAR(45)  NOT NULL                     │
├─────────────────────────────────────────────────────────────────┤
│ -- 상품 정보 --                                                   │
│ product_id            BIGINT NOT NULL                           │
│ product_name          VARCHAR(65) NOT NULL                      │
├─────────────────────────────────────────────────────────────────┤
│ -- 좌석 정보 --                                                   │
│ seat_id               BIGINT  NOT NULL                          │
│ seat_number           VARCHAR(30) NOT NULL                      │
├─────────────────────────────────────────────────────────────────┤
│ -- 결제 정보 --                                                   │
│ price                 BIGINT   NOT NULL                         │
├─────────────────────────────────────────────────────────────────┤
│ -- Audit --                                                     │
│ created_at            TIMESTAMP NOT NULL                        │
│ created_by            VARCHAR(255) NOT NULL                     │
│ updated_at            TIMESTAMP NOT NULL                        │
│ updated_by            VARCHAR(255) NOT NULL                     │
│ deleted_at            TIMESTAMP                                 │
│ deleted_by            VARCHAR(255)                              │
└─────────────────────────────────────────────────────────────────┘
```

## 관련 서비스/프로젝트

| 서비스                     | 역할       |
|-------------------------|----------|
| Product Service         | 상품 관리    |
| Ticket Service          | 티켓 관리    |
| Payment Service         | 결제 관리    |
| ReservationSeat Service | 예매 좌석 관리 |

---

© 2025 Tickatch Team
