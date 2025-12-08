package com.tickatch.reservationservice.reservation.application.dto;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationStatus;
import java.util.UUID;

public record ReservationResponse(
    UUID id, UUID reserverId, long productId, long seatId, Long price, ReservationStatus status,
    String reservationNumber) {

  public static ReservationResponse from(Reservation reservation) {
    return new ReservationResponse(
        reservation.getId().toUuid(),
        reservation.getReserver().getId(),
        reservation.getProductInfo().getProductId(),
        reservation.getProductInfo().getSeatId(),
        reservation.getProductInfo().getPrice(),
        reservation.getStatus(),
        reservation.getReservationNumber());
  }
}
