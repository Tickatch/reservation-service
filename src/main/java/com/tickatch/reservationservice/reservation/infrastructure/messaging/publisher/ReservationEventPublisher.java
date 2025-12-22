package com.tickatch.reservationservice.reservation.infrastructure.messaging.publisher;

import com.tickatch.reservationservice.reservation.application.event.ReservationCompletedEvent;
import com.tickatch.reservationservice.reservation.application.port.ReservationEventPublisherPort;
import com.tickatch.reservationservice.reservation.infrastructure.messaging.config.RabbitMQConfig;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationEventPublisher implements ReservationEventPublisherPort {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(ReservationCompletedEvent event) {
    IntegrationEvent integrationEvent = IntegrationEvent.from(event, "reservation-service");
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.RESERVATION_EXCHANGE,
        RabbitMQConfig.ROUTING_KEY_RESERVATION_COMPLETED_NOTIFICATION,
        integrationEvent);
  }
}
