package com.tickatch.reservationservice.reservation.infrastructure.client;

import com.tickatch.reservationservice.reservation.infrastructure.client.dto.ProductClientResponse;
import io.github.tickatch.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeignClient {

  // 상품 관련 정보 가져오기 - 상품 단건 조회 호출
  @GetMapping("/api/v1/products/{productId}")
  ApiResponse<ProductClientResponse> getProductInfo(@PathVariable Long productId);
}
