package com.tickatch.reservationservice.reservation.infrastructure.api;

import com.tickatch.reservationservice.reservation.domain.dto.ProductInformation;
import com.tickatch.reservationservice.reservation.domain.service.ProductService;
import com.tickatch.reservationservice.reservation.infrastructure.client.ProductFeignClient;
import com.tickatch.reservationservice.reservation.infrastructure.client.dto.ProductClientResponse;
import io.github.tickatch.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductFeignClient productFeignClient;

  @Override
  public ProductInformation getProduct(Long productId) {
    ApiResponse<ProductClientResponse> response = productFeignClient.getProductInfo(productId);
    ProductClientResponse data = response.getData();
    return data.toProductInformation();
  }
}
