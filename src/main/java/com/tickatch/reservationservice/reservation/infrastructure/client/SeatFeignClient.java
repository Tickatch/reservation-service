package com.tickatch.reservationservice.reservation.infrastructure.client;

import io.github.tickatch.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "reservation-seats")
public interface SeatFeignClient {

  // 예매
  @PostMapping("/{reservationSeatId}/reserve")
  ApiResponse<Void> reserve(@PathVariable long reservationSeatId);

  // 선점
  @PostMapping("/{reservationSeatId}/preempt")
  ApiResponse<Void> preempt(@PathVariable long reservationSeatId);

  // 취소
  @PostMapping("/{reservationSeatId}/cancel")
  ApiResponse<Void> cancel(@PathVariable long reservationSeatId);
}
