package com.tickatch.reservationservice.reservation.application.port;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReservationLogPort {
  void publishAction(
      UUID reservationId,
      String reservationNumber,
      String actionType,
      String actorType,
      UUID actorUserId,
      LocalDateTime occurredAt);
}
