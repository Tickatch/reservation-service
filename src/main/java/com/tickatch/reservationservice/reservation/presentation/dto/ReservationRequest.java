package com.tickatch.reservationservice.reservation.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReservationRequest(
    @NotNull UUID reserverId,
    @NotBlank String reserverName,
    long productId,
    @NotBlank String productName,
    long seatId,
    @NotBlank String seatNumber,
    Long price) {}
