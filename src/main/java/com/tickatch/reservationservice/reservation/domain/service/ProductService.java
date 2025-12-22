package com.tickatch.reservationservice.reservation.domain.service;

import com.tickatch.reservationservice.reservation.domain.dto.ProductInformation;

public interface ProductService {

  // 상품 정보 가져오기
  ProductInformation getProduct(Long productId);
}
