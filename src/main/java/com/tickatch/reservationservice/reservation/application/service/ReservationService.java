package com.tickatch.reservationservice.reservation.application.service;

import com.tickatch.reservationservice.reservation.application.dto.ReservationDetailResponse;
import com.tickatch.reservationservice.reservation.application.dto.ReservationRequest;
import com.tickatch.reservationservice.reservation.application.dto.ReservationResponse;
import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationErrorCode;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationException;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationDetailsRepository;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationRepository;
import com.tickatch.reservationservice.reservation.domain.service.SeatPreemptService;
import com.tickatch.reservationservice.reservation.domain.service.TicketService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final ReservationDetailsRepository reservationDetailsRepository;
  private final SeatPreemptService seatPreemptService;
  private final TicketService ticketService;

  // 1. 예매 생성
  @Transactional
  public ReservationResponse reserve(ReservationRequest req) {

    long seatId = req.seatId();

    // 1) 좌석 선점
    try {
      seatPreemptService.preempt(seatId);
    } catch (Exception e) {
      throw new ReservationException(ReservationErrorCode.SEAT_PREEMPT_FAILED);
    }

    // 2) 예매 엔티티 생성
    Reservation reservation;
    try {
      reservation =
          Reservation.create(
              req.reserverId(),
              req.reserverName(),
              req.productId(),
              req.productName(),
              req.seatId(),
              req.seatNumber(),
              req.price());

    } catch (Exception e) {
      // 좌석 선점 취소
      seatPreemptService.cancel(seatId);
      throw new ReservationException(ReservationErrorCode.RESERVATION_SAVE_FAILED);
    }

    //    // 3) 예매 확정
    //    try {
    //      seatPreemptService.reserve(seatId);
    //    } catch (Exception e) {
    //      // 예매 삭제 및 선점 취소
    //      reservationRepository.delete(reservation);
    //      seatPreemptService.cancel(seatId);
    //
    //      throw new ReservationException(ReservationErrorCode.SEAT_RESERVE_FAILED);
    //    }
    //
    //    // 4) 예매 확정으로 상태 변경
    //    reservation.paymentConfirm();
    //    reservationRepository.save(reservation);

    return ReservationResponse.from(reservation);
  }

  // 2. 예매 상세 조회
  @Transactional(readOnly = true)
  public ReservationDetailResponse getDetailReservation(UUID reservationId) {

    // 예매 id로 조회
    Reservation reservation =
        reservationRepository
            .findById(ReservationId.of(reservationId))
            .orElseThrow(
                () -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    return ReservationDetailResponse.from(reservation);
  }

  // 3. 예매 목록 조회
  @Transactional(readOnly = true)
  public Page<ReservationResponse> getAllReservations(UUID reserverId, Pageable pageable) {

    return reservationDetailsRepository
        .findAllByReserverId(reserverId, pageable)
        .map(ReservationResponse::from);
  }

  // 4. 예매 취소
  @Transactional
  public void cancel(UUID reservationId) {

    Reservation reservation =
        reservationRepository
            .findById(ReservationId.of(reservationId))
            .orElseThrow(
                () -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    // 예매 상태를 CANCEL로 변경
    reservation.cancel();

    // 좌석 선점 취소
    seatPreemptService.cancel(reservation.getProductInfo().getSeatId());

    // 예매 id에 해당하는 티켓 취소
    ticketService.cancel(reservationId);
  }

  // 5. 상품 취소 이벤트 처리
  @Transactional
  public void cancelByProductId(Long productId) {

    // 상품에 해당하는 예매 목록 조회
    List<Reservation> reservations =
        reservationRepository.findAllByProductInfo_ProductId(productId);

    // 해당 예매가 없는 경우
    if (reservations.isEmpty()) {
      log.info("예매 없음. productId={}", productId);
      return;
    }

    int cancelledCount = 0;
    for (Reservation r : reservations) {
      r.cancel();
      cancelledCount++;
    }

    log.info("총 {}건의 예매 취소 완료. productId={}", cancelledCount, productId);
  }

  // 6. 예매 확정 여부
  public boolean isConfirmed(UUID reservationId) {

    Reservation reservation =
        reservationRepository
            .findById(ReservationId.of(reservationId))
            .orElseThrow(
                () -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    return reservation.isConfirmed();
  }

  // 7. 예매 기한 만료 처리
  @Transactional
  public void expireReservations() {
    LocalDateTime now = LocalDateTime.now();

    // 현재를 기준으로 만료 예매 조회
    List<Reservation> targets = reservationRepository.findAllExpiredTargets(now);

    // 만료 상태로 변경 및 선점 취소
    for (Reservation reservation : targets) {
      reservation.expire(now);

      // 좌석 선점 취소
      try {
        seatPreemptService.cancel(reservation.getProductInfo().getSeatId());
      } catch (Exception e) {
        log.warn("좌석 선점 취소 실패 seatId={}", reservation.getProductInfo().getSeatId(), e);
      }
    }
    log.info("예매 기한 만료 시 좌석 선점 취소 성공");
  }

  // 8. 결제 상태에 따라 예매 확정/취소 처리
  @Transactional
  public void applyPaymentResult(String status, List<String> reservationIds) {

    // 결제 상태가 success인지 확인
    boolean isSuccess = "SUCCESS".equalsIgnoreCase(status);

    // 예매 id로 찾아서 예매 상태 변경
    for (String id : reservationIds) {
      Reservation reservation =
          reservationRepository
              .findById(ReservationId.of(UUID.fromString(id)))
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
    }
  }
}
