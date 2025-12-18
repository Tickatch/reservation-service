package com.tickatch.reservationservice.reservation.infrastructure.client.dto;

import java.util.List;

public record PaymentRefundRequest(String reason, List<String> reservationIds) {}
