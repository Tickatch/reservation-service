package com.tickatch.reservationservice.reservation.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tickatch.reservationservice.reservation.domain.dto.ProductInformation;
import java.time.LocalDateTime;

public record ProductClientResponse(
    @JsonProperty("id") Long productId,
    @JsonProperty("startAt") LocalDateTime performanceDate,
    String artHallName,
    String stageName) {

  public ProductInformation toProductInformation() {
    return new ProductInformation(productId, performanceDate, artHallName, stageName);
  }
}
