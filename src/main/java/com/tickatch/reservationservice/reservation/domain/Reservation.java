package com.tickatch.reservationservice.reservation.domain;

import static java.util.Objects.requireNonNull;

import com.tickatch.reservationservice.global.domain.AbstractAuditEntity;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationErrorCode;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationException;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "p_reservation")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends AbstractAuditEntity {

  //예매 id
  @EmbeddedId
  private ReservationId id;

  //예매자 id
  @Column(nullable = false)
  private UUID reserverId;

  //예매 상품 id
  @Column(nullable = false)
  private Long productId;

  //예매 좌석 id
  @Column(nullable = false)
  private Long seatId;

  //예매 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReservationStatus status;

  //예매 금액
  @Column(nullable = false)
  private Long price;

  //예매 번호
  @Column(nullable = false, unique = true)
  private String reservationNumber;

  //=======================================

  //생성

  //1. 예매 생성
  public static Reservation create(ReservationCreateRequest request) {
    Reservation reservation = new Reservation();
    reservation.id = ReservationId.generateId();
    reservation.reserverId = requireNonNull(request.reserverId());
    reservation.productId = requireNonNull(request.productId());
    reservation.seatId = requireNonNull(request.seatId());
    reservation.price = requireNonNull(request.price());
    reservation.reservationNumber = generateReservationNumber();
    reservation.status = ReservationStatus.INIT;
    return reservation;
  }

  //2. 예매번호(랜덤 문자열) 8자 생성
  private static String generateReservationNumber() {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  //=======================================

  //상태 변경

  //1. 결제 진행중
  public void startPayment() {
    validateCanceledOrExpired();
    this.status = ReservationStatus.PENDING_PAYMENT;
  }

  //2. 결제 성공
  public void confirmSeats() {
    validatePaymentPending();
    this.status = ReservationStatus.CONFIRMED;
  }

  //3. 결제 실패
  public void paymentFailed() {
    validatePaymentPending();
    this.status = ReservationStatus.PAYMENT_FAILED;
    //좌석 원복 처리 필요
  }

  //4. 사용자 예매 취소
  public void cancel() {
    validateCanceledOrExpired();
    this.status = ReservationStatus.CANCELED;
    //좌석 원복 처리 필요
  }

  //5. 결제 시간 만료
  public void expire() {
    validatePaymentPending();
    this.status = ReservationStatus.EXPIRED;
    //좌석 원복 처리 필요
  }

  //=======================================

  //검증

  //1. 결제 진행 상태인지 확인
  private void validatePaymentPending() {
    if (this.status != ReservationStatus.PENDING_PAYMENT) {
      throw new ReservationException(ReservationErrorCode.INVALID_STATUS_FOR_PAYMENT);
    }
  }

  //2. 취소되었거나 만료된 상태인지 확인
  private void validateCanceledOrExpired() {
    if (this.status == ReservationStatus.CANCELED || this.status == ReservationStatus.EXPIRED) {
      throw new ReservationException(ReservationErrorCode.ALREADY_CANCELED_OR_EXPIRED);
    }
  }
}
