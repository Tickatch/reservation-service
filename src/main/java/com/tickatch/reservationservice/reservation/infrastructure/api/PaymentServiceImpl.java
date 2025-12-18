package com.tickatch.reservationservice.reservation.infrastructure.api;

import com.tickatch.reservationservice.reservation.domain.service.PaymentService;
import com.tickatch.reservationservice.reservation.infrastructure.client.PaymentFeignClient;
import com.tickatch.reservationservice.reservation.infrastructure.client.dto.PaymentRefundRequest;
import io.github.tickatch.common.api.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  public final PaymentFeignClient paymentFeignClient;

  @Override
  public void refund(String reason, List<String> reservationIds) {

    PaymentRefundRequest request = new PaymentRefundRequest(reason, reservationIds);
    ApiResponse<Void> response = paymentFeignClient.refund(request);

    if (!response.isSuccess()) {
      throw new IllegalStateException(
          String.format("Payment refund failed: %s", response.getError()));
    }
  }
}
