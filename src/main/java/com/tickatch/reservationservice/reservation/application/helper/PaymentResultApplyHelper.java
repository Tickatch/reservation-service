package com.tickatch.reservationservice.reservation.application.helper;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationErrorCode;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationException;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationRepository;
import com.tickatch.reservationservice.reservation.domain.service.SeatPreemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultApplyHelper {

  private final ReservationRepository reservationRepository;
  private final SeatPreemptService seatPreemptService;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void applySafely(ReservationId reservationId, boolean isSuccess) {
    try {
      Reservation reservation =
          reservationRepository
              .findById(reservationId)
              .orElseThrow(
                  () -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

      if (isSuccess) {
        // 예매 확정 상태로 변경 후 좌석 예매
        reservation.paymentConfirm();
        seatPreemptService.reserve(reservation.getProductInfo().getSeatId());
      } else {
        // 예매 실패 상태로 변경 후 선점 좌석 취소
        reservation.paymentFailed();
        seatPreemptService.cancel(reservation.getProductInfo().getSeatId());
      }

      log.info("결제 결과 반영 성공 reservationId={}, success={}", reservationId, isSuccess);

    } catch (Exception e) {
      log.error("결제 결과 반영 실패 reservationId={}, success={}", reservationId, isSuccess, e);
    }
  }
}
