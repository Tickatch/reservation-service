package com.tickatch.reservationservice.reservation.domain.dto;

import java.util.UUID;

public record UserInformation(UUID reserverId, String email) {}
