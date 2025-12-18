package com.tickatch.reservationservice.reservation.application.event;

import com.tickatch.reservationservice.reservation.domain.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationRefundEventListener {

  private final PaymentService paymentService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void refundAfterCancel(ReservationCanceledEvent event) {
    try {
      paymentService.refund(event.reason(), event.reservationIds());
    } catch (Exception e) {
      log.error("[PAYMENT-REFUND-FAIL] reservationIds={}", event.reservationIds(), e);
    }
  }
}
