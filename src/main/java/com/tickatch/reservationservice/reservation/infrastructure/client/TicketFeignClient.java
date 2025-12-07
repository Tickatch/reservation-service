package com.tickatch.reservationservice.reservation.infrastructure.client;

import io.github.tickatch.common.api.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "ticket-service")
public interface TicketFeignClient {

  // 티켓 취소
  @PostMapping("/api/v1/tickets/{reservationId}/cancel")
  ApiResponse<Void> cancelTicketByReservation(@PathVariable UUID reservationId);
}
