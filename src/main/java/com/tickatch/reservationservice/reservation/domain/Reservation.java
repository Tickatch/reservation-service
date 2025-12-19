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
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@Table(name = "p_reservation")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends AbstractAuditEntity {

  // 예매 id
  @EmbeddedId private ReservationId id;

  // 예매자
  @Embedded private Reserver reserver;

  // 상품 정보
  @Embedded private ProductInfo productInfo;

  // 예매 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReservationStatus status;

  // 예매 번호
  @Column(nullable = false, unique = true)
  private String reservationNumber;

  // 만료 시각
  @Column private LocalDateTime expireAt;

  // =======================================

  // 생성

  // 1. 예매 생성
  @Builder(access = AccessLevel.PRIVATE)
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
    this.expireAt = LocalDateTime.now().plusMinutes(10);
  }

  // 팩토리 메서드
  public static Reservation create(
      UUID reserverId,
      String reserverName,
      long productId,
      String productName,
      long seatId,
      String seatNumber,
      Long price) {
    return Reservation.builder()
        .reserverId(reserverId)
        .reserverName(reserverName)
        .productId(productId)
        .productName(productName)
        .seatId(seatId)
        .seatNumber(seatNumber)
        .price(price)
        .build();
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
  // 최초 생성 상태일 때만 결제 진행중 상태로 넘어갈 수 있다.
  public void startPayment() {
    validateStartPayment();
    this.status = ReservationStatus.PENDING_PAYMENT;
  }

  // 2. 결제 실패
  // 결제 진행 상태 이후에만 결제 실패로 넘어갈 수 있다.
  public void paymentFailed() {
    validatePaymentPending();
    this.status = ReservationStatus.PAYMENT_FAILED;
  }

  // 3. 사용자 예매 취소(예매 확정 전)
  // 취소 또는 만료 상태가 아닐 때만 취소할 수 있다.
  public void cancel() {
    validateCanceledOrExpired();
    this.status = ReservationStatus.CANCELED;
  }

  // 4. 결제 성공으로 예매 확정 상태로 변경
  // 결제 진행 상태 이후에만 예매 확정으로 넘어갈 수 있다.
  public void paymentConfirm() {
    validatePaymentPending();
    this.status = ReservationStatus.CONFIRMED;
  }

  // 5. 예매 시간 만료
  // 예매 확정, 취소, 만료 상태가 아닌 경우에만 만료로 넘어갈 수 있다.
  public void expire(LocalDateTime now) {
    validateCanExpired();

    if (now.isBefore(this.expireAt)) {
      throw new ReservationException(ReservationErrorCode.EXPIRE_TIME_NOT_REACHED);
    }

    this.status = ReservationStatus.EXPIRED;
  }

  // 6. 현재 confirm 상태인지
  public boolean isConfirmed() {
    return this.status == ReservationStatus.CONFIRMED;
  }

  // 7. 환불이 필요한 예매 취소(예매 확정 후 취소)
  public void cancelWithRefund() {
    validateCancelableAfterPayment();

    log.info("before cancel status={}", this.status);
    this.status = ReservationStatus.CANCELED;
    log.info("after cancel status={}", this.status);
  }

  // =======================================

  // 검증

  // 1. 결제 진행이 가능한 상태인지 확인
  private void validateStartPayment() {
    if (this.status != ReservationStatus.INIT) {
      throw new ReservationException(ReservationErrorCode.INVALID_STATUS_FOR_PAYMENT_START);
    }
  }

  // 2. 결제 진행 상태인지 확인
  private void validatePaymentPending() {
    if (this.status != ReservationStatus.PENDING_PAYMENT) {
      throw new ReservationException(ReservationErrorCode.STATUS_IS_NOT_PAYMENT_PENDING);
    }
  }

  // 3. 취소되었거나 만료된 상태인지 확인
  private void validateCanceledOrExpired() {
    if (this.status == ReservationStatus.CANCELED || this.status == ReservationStatus.EXPIRED) {
      throw new ReservationException(ReservationErrorCode.ALREADY_CANCELED_OR_EXPIRED);
    }
  }

  // 4. 만료 가능한 상태인지 확인
  private void validateCanExpired() {
    if (this.status == ReservationStatus.CONFIRMED
        || this.status == ReservationStatus.CANCELED
        || this.status == ReservationStatus.EXPIRED) {
      throw new ReservationException(ReservationErrorCode.INVALID_STATUS_FOR_EXPIRE);
    }
  }

  // 5. 환불이 필요한 예매 취소 가능한지 확인
  // 예매 확정 이후에만 환불을 동반한 예매 취소 가능
  private void validateCancelableAfterPayment() {
    if (this.status != ReservationStatus.CONFIRMED) {
      throw new ReservationException(ReservationErrorCode.INVALID_STATUS_FOR_REFUND_CANCEL);
    }
  }
}
