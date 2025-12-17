package com.tickatch.reservationservice.reservation.infrastructure.client;

import io.github.tickatch.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "reservation-seat-service")
public interface SeatFeignClient {

  // 예매
  @PostMapping("/api/v1/reservation-seats/{reservationSeatId}/reserve")
  ApiResponse<Void> reserve(@PathVariable long reservationSeatId);

  // 선점
  @PostMapping("/api/v1/reservation-seats/{reservationSeatId}/preempt")
  ApiResponse<Void> preempt(@PathVariable long reservationSeatId);

  // 취소
  @PostMapping("/api/v1/reservation-seats/{reservationSeatId}/cancel")
  ApiResponse<Void> cancel(@PathVariable long reservationSeatId);
}
