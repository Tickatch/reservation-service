package com.tickatch.reservationservice.reservation.application.scheduler;

import com.tickatch.reservationservice.reservation.application.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpireScheduler {

  private final ReservationService reservationService;

  @Scheduled(fixedRate = 60000)
  public void runExpiredReservations() {
    reservationService.expireReservations();

    log.info("만료된 예매 처리 완료");
  }
}
