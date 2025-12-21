package com.tickatch.reservationservice.reservation.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tickatch.reservationservice.reservation.domain.dto.UserInformation;
import java.util.UUID;

public record UserClientResponse(@JsonProperty("id") UUID userId, String email) {

  public UserInformation toUserInformation() {
    return new UserInformation(userId, email);
  }
}
