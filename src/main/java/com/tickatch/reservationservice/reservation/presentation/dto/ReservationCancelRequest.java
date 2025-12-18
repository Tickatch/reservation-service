package com.tickatch.reservationservice.reservation.presentation.dto;

import com.tickatch.reservationservice.reservation.application.dto.CancelRequest;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record ReservationCancelRequest(@NotEmpty List<UUID> reservationIds) {

  public CancelRequest toCancelRequest() {
    return new CancelRequest(reservationIds);
  }
}
