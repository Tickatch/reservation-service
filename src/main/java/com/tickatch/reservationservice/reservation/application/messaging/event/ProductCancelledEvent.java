package com.tickatch.reservationservice.reservation.application.messaging.event;

import io.github.tickatch.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductCancelledEvent extends DomainEvent {

  private Long productId;

  public ProductCancelledEvent(Long productId) {
    super();
    this.productId = productId;
  }

  @Override
  public String getAggregateId() {
    return String.valueOf(productId);
  }

  @Override
  public String getAggregateType() {
    return "Product";
  }

  @Override
  public String getRoutingKey() {
    return "product.cancelled.reservation";
  }
}
