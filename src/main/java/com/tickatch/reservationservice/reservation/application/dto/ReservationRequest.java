package com.tickatch.reservationservice.reservation.application.dto;

import java.util.UUID;

public record ReservationRequest(
    UUID reserverId,
    String reserverName,
    long productId,
    String productName,
    long seatId,
    String seatNumber,
    Long price) {

}
