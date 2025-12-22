package com.tickatch.reservationservice.reservation.application.dto.request;

import java.util.List;
import java.util.UUID;

public record PendingPaymentRequest(List<String> reservationIds) {

  public List<UUID> toUUIDs() {
    return reservationIds().stream().map(UUID::fromString).toList();
  }
}
