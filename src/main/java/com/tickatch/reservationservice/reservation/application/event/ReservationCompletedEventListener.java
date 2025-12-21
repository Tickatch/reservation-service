package com.tickatch.reservationservice.reservation.application.event;

import com.tickatch.reservationservice.reservation.application.port.ReservationEventPublisherPort;
import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import com.tickatch.reservationservice.reservation.domain.dto.ProductInformation;
import com.tickatch.reservationservice.reservation.domain.dto.UserInformation;
import com.tickatch.reservationservice.reservation.domain.event.ReservationCompletedDomainEvent;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationRepository;
import com.tickatch.reservationservice.reservation.domain.service.ProductService;
import com.tickatch.reservationservice.reservation.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCompletedEventListener {

  private final ReservationRepository reservationRepository;
  private final UserService userService;
  private final ProductService productService;
  private final ReservationEventPublisherPort reservationEventPublisher;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(ReservationCompletedDomainEvent event) {

    ReservationId reservationId = ReservationId.of(event.reservationId());

    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

    // 1. reserverId로 user 정보 가져오기
    UserInformation user = userService.getUser(reservation.getReserver().getId());

    // 2. productId로 product 정보 가져오기
    ProductInformation product =
        productService.getProduct(reservation.getProductInfo().getProductId());

    // 3. 이벤트 생성
    ReservationCompletedEvent createdEvent =
        ReservationCompletedEvent.of(reservation, user, product);

    // 4. RabbitMq 이벤트 발행
    try {
      reservationEventPublisher.publish(createdEvent);
    } catch (Exception e) {
      log.error("ReservationCreatedEvent 발행 실패. reservationId={}", event.reservationId(), e);
    }
  }
}
