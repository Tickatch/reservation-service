package com.tickatch.reservationservice.reservation.application.dto;

import java.util.List;
import java.util.UUID;

public record CancelRequest(List<UUID> reservationIds) {}
