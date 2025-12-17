package com.tickatch.reservationservice.reservation.domain.service;

import java.util.UUID;

public interface TicketService {

  // 티켓 취소
  void cancel(UUID reservationId);
}
