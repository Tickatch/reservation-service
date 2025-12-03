package com.tickatch.reservationservice.reservation.domain;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReservationCreateRequest(
    @NotNull UUID reserverId,
    @NotNull Long productId,
    @NotNull Long seatId,
    @NotNull Long price
) {

}
