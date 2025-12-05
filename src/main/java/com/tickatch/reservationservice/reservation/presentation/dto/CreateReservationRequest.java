package com.tickatch.reservationservice.reservation.presentation.dto;

import com.tickatch.reservationservice.reservation.application.dto.ReservationRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateReservationRequest(
    @NotNull UUID reserverId,
    @NotBlank String reserverName,
    long productId,
    @NotBlank String productName,
    long seatId,
    @NotBlank String seatNumber,
    Long price
) {

  public ReservationRequest toReservationRequest() {
    return new ReservationRequest(
        this.reserverId,
        this.reserverName,
        this.productId,
        this.productName,
        this.seatId,
        this.seatNumber,
        this.price
    );
  }
}
