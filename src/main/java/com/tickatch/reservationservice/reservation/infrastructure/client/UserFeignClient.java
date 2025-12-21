package com.tickatch.reservationservice.reservation.infrastructure.client;

import com.tickatch.reservationservice.reservation.infrastructure.client.dto.UserClientResponse;
import io.github.tickatch.common.api.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

  // 수령자 이메일 가져오기
  @GetMapping("/api/v1/user/customers/{reserverId}")
  ApiResponse<UserClientResponse> getRecipientInfo(@PathVariable UUID reserverId);
}
