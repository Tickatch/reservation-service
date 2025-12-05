package com.tickatch.reservationservice.reservation.infrastructure.messaging.consumer;

import com.tickatch.reservationservice.reservation.application.messaging.event.ProductCancelledEvent;
import com.tickatch.reservationservice.reservation.application.service.ReservationService;
import com.tickatch.reservationservice.reservation.infrastructure.messaging.config.RabbitMQConfig;
import io.github.tickatch.common.event.EventContext;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCancelledEventConsumer {

  private final ReservationService reservationService;

  /**
   * 상품 취소 이벤트를 수신하여 예약을 취소 처리한다.
   *
   * @param integrationEvent 통합 이벤트
   */
  @RabbitListener(queues = RabbitMQConfig.QUEUE_PRODUCT_CANCELLED_RESERVATION)
  public void handleProductCancelled(IntegrationEvent integrationEvent) {
    log.info(
        "상품 취소 이벤트 수신. eventId: {}, traceId: {}",
        integrationEvent.getEventId(),
        integrationEvent.getTraceId());

    EventContext.run(
        integrationEvent,
        event -> {
          ProductCancelledEvent payload = event.getPayloadAs(ProductCancelledEvent.class);

          log.info("예약 취소 처리 시작. productId: {}", payload.getProductId());
          reservationService.cancelByProductId(payload.getProductId());
          log.info("예약 취소 처리 완료. productId: {}", payload.getProductId());
        });
  }
}
