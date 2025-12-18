package com.tickatch.reservationservice.reservation.application.helper;

import com.tickatch.reservationservice.reservation.domain.service.TicketService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCancelHelper {

  private final TicketService ticketService;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void cancelTicketSafely(UUID reservationId) {
    try {
      ticketService.cancel(reservationId);
    } catch (Exception e) {
      log.error("티켓 취소 실패 reservationId={}", reservationId, e);
    }
  }
}
