package com.tickatch.reservationservice.reservation.presentation.dto;

import java.util.List;

public record PaymentResultRequest(String status, List<String> reservationIds) {}
