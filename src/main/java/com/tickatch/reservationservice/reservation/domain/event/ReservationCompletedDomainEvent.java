package com.tickatch.reservationservice.reservation.domain.event;

import java.util.UUID;

public record ReservationCompletedDomainEvent(UUID reservationId) {}
