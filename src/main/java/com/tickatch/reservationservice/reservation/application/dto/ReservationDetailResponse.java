package com.tickatch.reservationservice.reservation.application.dto;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationStatus;
import java.util.UUID;

public record ReservationDetailResponse(
    UUID id,
    UUID reserverId,
    String reserverName,
    long productId,
    String productName,
    long seatId,
    String seatNumber,
    Long price,
    ReservationStatus status) {

  public static ReservationDetailResponse from(Reservation reservation) {
    return new ReservationDetailResponse(
        reservation.getId().toUuid(),
        reservation.getReserver().getId(),
        reservation.getReserver().getName(),
        reservation.getProductInfo().getProductId(),
        reservation.getProductInfo().getProductName(),
        reservation.getProductInfo().getSeatId(),
        reservation.getProductInfo().getSeatNumber(),
        reservation.getProductInfo().getPrice(),
        reservation.getStatus());
  }
}
