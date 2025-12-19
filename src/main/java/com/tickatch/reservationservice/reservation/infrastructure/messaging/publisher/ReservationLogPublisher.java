package com.tickatch.reservationservice.reservation.infrastructure.messaging.publisher;

import com.tickatch.reservationservice.reservation.application.port.ReservationLogPort;
import com.tickatch.reservationservice.reservation.infrastructure.messaging.config.RabbitMQConfig;
import com.tickatch.reservationservice.reservation.infrastructure.messaging.event.ReservationLogEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationLogPublisher implements ReservationLogPort {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publishAction(
      UUID reservationId,
      String reservationNumber,
      String actionType,
      String actorType,
      UUID actorUserId,
      LocalDateTime occurredAt) {

    ReservationLogEvent event =
        new ReservationLogEvent(
            UUID.randomUUID(),
            reservationId,
            reservationNumber,
            actionType,
            actorType,
            actorUserId,
            occurredAt);

    rabbitTemplate.convertAndSend(
        RabbitMQConfig.LOG_EXCHANGE, RabbitMQConfig.ROUTING_KEY_RESERVATION_LOG, event);
  }
}
