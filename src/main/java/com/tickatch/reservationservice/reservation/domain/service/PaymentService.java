package com.tickatch.reservationservice.reservation.domain.service;

import java.util.List;

public interface PaymentService {

  // 환불 처리
  void refund(String reason, List<String> reservationIds);
}
