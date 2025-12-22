package com.tickatch.reservationservice.reservation.domain.dto;

import java.time.LocalDateTime;

public record ProductInformation(
    Long productId, // product.id
    LocalDateTime performanceDate, // product.startAt
    String artHallName,
    String stageName) {}
