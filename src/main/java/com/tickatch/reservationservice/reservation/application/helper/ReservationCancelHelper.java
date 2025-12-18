package com.tickatch.reservationservice.reservation.application.helper;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.service.SeatPreemptService;
import com.tickatch.reservationservice.reservation.domain.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCancelHelper {

  private final SeatPreemptService seatPreemptService;
  private final TicketService ticketService;

  /**
   * 외부 리소스(좌석, 티켓) 취소를 안전하게 수행한다. - 각 호출은 독립 트랜잭션(REQUIRES_NEW) - 실패해도 예외를 밖으로 던지지 않는다
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void cancelExternalResources(Reservation reservation) {

    try {
      // 좌석 선점 취소
      seatPreemptService.cancel(reservation.getProductInfo().getSeatId());
    } catch (Exception e) {
      log.error("좌석 취소 실패 reservationId={}", reservation.getId(), e);
    }

    try {
      // 예매 id에 해당하는 티켓 취소
      ticketService.cancel(reservation.getId().toUuid());
    } catch (Exception e) {
      log.error("티켓 취소 실패 reservationId={}", reservation.getId(), e);
    }

    log.info("외부 리소스 취소 성공 reservationId={}", reservation.getId());
  }
}
