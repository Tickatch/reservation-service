package com.tickatch.reservationservice.reservation.application.port;

import com.tickatch.reservationservice.reservation.application.event.ReservationCompletedEvent;

public interface ReservationEventPublisherPort {

  void publish(ReservationCompletedEvent event);
}
