package com.tickatch.reservationservice.reservation.application.service;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationErrorCode;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationException;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationDetailsRepository;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationRepository;
import com.tickatch.reservationservice.reservation.domain.service.SeatPreemptService;
import com.tickatch.reservationservice.reservation.presentation.dto.ReservationDetailResponse;
import com.tickatch.reservationservice.reservation.presentation.dto.ReservationRequest;
import com.tickatch.reservationservice.reservation.presentation.dto.ReservationResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final ReservationDetailsRepository reservationDetailsRepository;
  private final SeatPreemptService seatPreemptService;

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
          Reservation.builder()
              .reserverId(req.reserverId())
              .reserverName(req.reserverName())
              .productId(req.productId())
              .productName(req.productName())
              .price(req.price())
              .seatId(req.seatId())
              .seatNumber(req.seatNumber())
              .build();

      reservationRepository.save(reservation);
    } catch (Exception e) {
      // 좌석 선점 취소
      seatPreemptService.cancel(seatId);
      throw new ReservationException(ReservationErrorCode.RESERVATION_SAVE_FAILED);
    }

    // 3) 예매 확정
    try {
      seatPreemptService.reserve(seatId);
    } catch (Exception e) {
      // 예매 삭제 및 선점 취소
      reservationRepository.delete(reservation);
      seatPreemptService.cancel(seatId);

      throw new ReservationException(ReservationErrorCode.SEAT_RESERVE_FAILED);
    }

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
  }
}
