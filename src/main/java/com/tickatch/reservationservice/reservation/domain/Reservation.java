package com.tickatch.reservationservice.reservation.domain;

import com.tickatch.reservationservice.global.domain.AbstractAuditEntity;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationErrorCode;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Table(name = "p_reservation")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends AbstractAuditEntity {

  // 예매 id
  @EmbeddedId
  private ReservationId id;

  // 예매자
  @Embedded
  private Reserver reserver;

  // 상품 정보
  @Embedded
  private ProductInfo productInfo;

  // 예매 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReservationStatus status;

  // 예매 번호
  @Column(nullable = false, unique = true)
  private String reservationNumber;

  // =======================================

  // 생성

  // 1. 예매 생성
  @Builder
  public Reservation(
      UUID reservationId,
      UUID reserverId,
      String reserverName,
      long productId,
      String productName,
      long seatId,
      String seatNumber,
      Long price) {
    this.id = ReservationId.of(reservationId);
    this.reserver = new Reserver(reserverId, reserverName);

    // 예매 진행
    this.productInfo =
        ProductInfo.builder()
            .price(price)
            .seatId(seatId)
            .seatNumber(seatNumber)
            .productId(productId)
            .productName(productName)
            .build();

    this.reservationNumber =
        StringUtils.hasText(reservationNumber) ? reservationNumber : generateReservationNumber();
    this.status = ReservationStatus.INIT;
  }

  // 2. 예매번호(날짜와 랜덤 문자열 조합) 10자 생성
  private String generateReservationNumber() {
    // 1) 날짜
    String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

    // 2) 랜덤 4자리 문자열 생성
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    Random random = new Random();
    StringBuilder randomPart = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      randomPart.append(chars.charAt(random.nextInt(chars.length())));
    }

    // 3) 날짜와 랜덤 문자열 조합
    return datePart + randomPart; // ex: 251203A7F3
  }

  // =======================================

  // 상태 관련

  // 1. 결제 진행중
  public void startPayment() {
    validateCanceledOrExpired();
    this.status = ReservationStatus.PENDING_PAYMENT;
  }

  // 2. 결제 성공
  public void confirmSeats() {
    validatePaymentPending();
    this.status = ReservationStatus.CONFIRMED;
  }

  // 3. 결제 실패
  public void paymentFailed() {
    validatePaymentPending();
    this.status = ReservationStatus.PAYMENT_FAILED;
    // 좌석 원복 처리 필요
  }

  // 4. 사용자 예매 취소
  public void cancel() {
    validateCanceledOrExpired();
    this.status = ReservationStatus.CANCELED;
  }

  // 5. 결제 시간 만료
  public void expire() {
    validatePaymentPending();
    this.status = ReservationStatus.EXPIRED;
    // 좌석 원복 처리 필요
  }

  // 6. 현재 confirm 상태인지
  public boolean isConfirmed() {
    return this.status == ReservationStatus.CONFIRMED;
  }

  // =======================================

  // 검증

  // 1. 결제 진행 상태인지 확인
  private void validatePaymentPending() {
    if (this.status != ReservationStatus.PENDING_PAYMENT) {
      throw new ReservationException(ReservationErrorCode.INVALID_STATUS_FOR_PAYMENT);
    }
  }

  // 2. 취소되었거나 만료된 상태인지 확인
  private void validateCanceledOrExpired() {
    if (this.status == ReservationStatus.CANCELED || this.status == ReservationStatus.EXPIRED) {
      throw new ReservationException(ReservationErrorCode.ALREADY_CANCELED_OR_EXPIRED);
    }
  }
}
