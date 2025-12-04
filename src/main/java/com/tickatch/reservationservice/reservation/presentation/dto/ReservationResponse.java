package com.tickatch.reservationservice.reservation.presentation.dto;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import java.util.UUID;

public record ReservationResponse(
    UUID id,
    UUID reserverId,
    long productId,
    long seatId,
    Long price
) {

  public static ReservationResponse from(Reservation reservation) {
    return new ReservationResponse(
        reservation.getId().toUuid(),
        reservation.getReserver().getId(),
        reservation.getProductInfo().getProductId(),
        reservation.getProductInfo().getSeatId(),
        reservation.getProductInfo().getPrice()
    );
  }
}
