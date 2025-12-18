package com.tickatch.reservationservice.reservation.application.event;

import java.util.List;

public record ReservationCanceledEvent(List<String> reservationIds, String reason) {}
