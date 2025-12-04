package com.tickatch.reservationservice.reservation.application.service;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import com.tickatch.reservationservice.reservation.domain.SeatAvailableCheck;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationErrorCode;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationException;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationRepository;
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

  private final SeatAvailableCheck seatAvailableCheck;
  private final ReservationRepository reservationRepository;

  //1. 예매 생성
  @Transactional
  public UUID reserve(ReservationRequest req) {

    Reservation reservation = Reservation.builder()
        .reserverId(req.reserverId())
        .reserverName(req.reserverName())
        .productId(req.productId())
        .productName(req.productName())
        .price(req.price())
        .seatId(req.seatId())
        .seatNumber(req.seatNumber())
        .seatAvailableCheck(seatAvailableCheck)
        .build();

    reservationRepository.save(reservation);

    return reservation.getId().toUuid();
  }

  //2. 예매 상세 조회
  @Transactional(readOnly = true)
  public ReservationDetailResponse getDetailReservation(UUID reservationId) {

    Reservation reservation = reservationRepository.findById(ReservationId.of(reservationId))
        .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    return ReservationDetailResponse.from(reservation);
  }

  //3. 예매 목록 조회
  @Transactional(readOnly = true)
  public Page<ReservationResponse> getAllReservations(Pageable pageable) {

    return reservationRepository.findAll(pageable)
        .map(ReservationResponse::from);
  }

  //4. 예매 취소
  @Transactional
  public void cancel(UUID reservationId) {

    Reservation reservation = reservationRepository.findById(ReservationId.of(reservationId))
        .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    reservation.cancel();
  }
}
