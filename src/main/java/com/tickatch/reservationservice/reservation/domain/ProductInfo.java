package com.tickatch.reservationservice.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductInfo {

  // 예매 상품 id
  private long productId;

  // 상품명
  @Column(nullable = false, length = 65)
  private String productName;

  // 예매 좌석 id
  private long seatId;

  // 좌석 번호
  @Column(nullable = false, length = 30)
  private String seatNumber;

  // 예매 금액
  private Long price;

  @Builder
  protected ProductInfo(
      long productId, String productName, long seatId, String seatNumber, long price) {
    this.productId = productId;
    this.productName = productName;
    this.seatId = seatId;
    this.seatNumber = seatNumber;
    this.price = price;
  }
}
