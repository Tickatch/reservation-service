package com.tickatch.reservationservice.reservation.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationLogEvent(
    UUID eventId,
    UUID reservationId,
    String reservationNumber,
    String actionType, // CREATED / CONFIRMED / CANCELED_BY_USER / CANCELED_BY_PRODUCT / EXPIRED
    String actorType,
    UUID actorUserId,
    LocalDateTime occurredAt) {}
