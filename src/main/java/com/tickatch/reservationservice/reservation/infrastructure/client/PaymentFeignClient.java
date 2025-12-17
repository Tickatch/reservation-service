package com.tickatch.reservationservice.reservation.infrastructure.client;

import com.tickatch.reservationservice.reservation.infrastructure.client.dto.PaymentRefundRequest;
import io.github.tickatch.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentFeignClient {

  // 환불
  @PostMapping("/api/v1/payments/refund")
  ApiResponse<Void> refund(@Valid @RequestBody PaymentRefundRequest request);
}
