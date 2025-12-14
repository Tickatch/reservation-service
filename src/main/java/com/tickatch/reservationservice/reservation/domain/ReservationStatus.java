package com.tickatch.reservationservice.reservation.domain;

public enum ReservationStatus {
  INIT, // 예매 최초 생성
  CONFIRMED, // 결제 승인(예매 확정)
  PAYMENT_FAILED, // 결제 실패
  CANCELED, // 사용자 취소
  EXPIRED // 예매 시간 만료로 인한 취소
}
