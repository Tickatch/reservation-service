package com.tickatch.reservationservice.reservation.application.helper;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationErrorCode;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationException;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationRepository;
import com.tickatch.reservationservice.reservation.domain.service.SeatPreemptService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpireHelper {

  private final ReservationRepository reservationRepository;
  private final SeatPreemptService seatPreemptService;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean expireSafely(ReservationId reservationId, LocalDateTime now) {

    try {
      Reservation reservation =
          reservationRepository.findById(reservationId)
              .orElseThrow(
                  () -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

      // 기한 만료로 상태 변경
      reservation.expire(now);

      // 좌석 선점 취소
      try {
        seatPreemptService.cancel(reservation.getProductInfo().getSeatId());
      } catch (Exception e) {
        log.warn("좌석 선점 취소 실패 seatId={}", reservation.getProductInfo().getSeatId(), e);
      }

      return true;
    } catch (Exception e) {
      log.error("예매 만료 처리 실패 reservationId={}", reservationId, e);
      return false;
    }
  }
}
