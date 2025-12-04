package com.tickatch.reservationservice.reservation.domain.service;

public interface SeatPreemptService {

  // 예매
  void reserve(long seatId);

  // 선점
  void preempt(long seatId);

  // 취소
  void cancel(long seatId);
}
