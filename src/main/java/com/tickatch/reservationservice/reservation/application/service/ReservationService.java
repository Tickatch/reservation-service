package com.tickatch.reservationservice.reservation.application.service;

import com.tickatch.reservationservice.global.config.AuthExtractor.AuthInfo;
import com.tickatch.reservationservice.global.security.ActorExtractor;
import com.tickatch.reservationservice.reservation.application.dto.request.ReservationRequest;
import com.tickatch.reservationservice.reservation.application.dto.response.ReservationDetailResponse;
import com.tickatch.reservationservice.reservation.application.dto.response.ReservationResponse;
import com.tickatch.reservationservice.reservation.application.event.ReservationCanceledEvent;
import com.tickatch.reservationservice.reservation.application.helper.PaymentResultApplyHelper;
import com.tickatch.reservationservice.reservation.application.helper.ProductCancelHelper;
import com.tickatch.reservationservice.reservation.application.helper.ReservationCancelHelper;
import com.tickatch.reservationservice.reservation.application.helper.ReservationExpireHelper;
import com.tickatch.reservationservice.reservation.application.port.ReservationLogPort;
import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationErrorCode;
import com.tickatch.reservationservice.reservation.domain.exception.ReservationException;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationRepository;
import com.tickatch.reservationservice.reservation.domain.service.SeatPreemptService;
import com.tickatch.reservationservice.reservation.domain.service.TicketService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final SeatPreemptService seatPreemptService;
  private final TicketService ticketService;
  private final ApplicationEventPublisher eventPublisher;
  private final ReservationExpireHelper expireHelper;
  private final PaymentResultApplyHelper paymentResultApplyHelper;
  private final ReservationCancelHelper reservationCancelHelper;
  private final ProductCancelHelper productCancelHelper;
  private final ReservationLogPort reservationLogPort;

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

    reservationRepository.save(reservation);

    // 예매 생성 로그 이벤트 발행
    try {
      ActorExtractor.ActorInfo actor = ActorExtractor.extract();

      reservationLogPort.publishAction(
          reservation.getId().toUuid(),
          reservation.getReservationNumber(),
          "CREATED",
          actor.actorType(),
          actor.actorUserId(),
          LocalDateTime.now());
    } catch (Exception e) {
      log.warn("예매 생성 로그 발행 실패. reservationId={}", reservation.getId(), e);
    }

    return ReservationResponse.from(reservation);
  }

  // 결제 시작 시 예매 상태를 결제 진행중으로 변환
  @Transactional
  public void markPendingPayment(List<UUID> reservationIds) {

    // 1) 예매 조회
    List<ReservationId> ids = reservationIds.stream().map(ReservationId::of).toList();

    List<Reservation> reservations = reservationRepository.findAllByIdIn(ids);

    if (reservations.size() != reservationIds.size()) {
      throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }

    reservations.forEach(
        reservation -> {
          reservation.startPayment();

          // 결제 진행중(PENDING_PAYMENT) 로그 이벤트 발행
          try {
            ActorExtractor.ActorInfo actor = ActorExtractor.extract();

            reservationLogPort.publishAction(
                reservation.getId().toUuid(),
                reservation.getReservationNumber(),
                "PENDING_PAYMENT",
                actor.actorType(),
                actor.actorUserId(),
                LocalDateTime.now());
          } catch (Exception e) {
            log.warn("결제 진행중 로그 발행 실패. reservationId={}", reservation.getId(), e);
          }
        });
  }

  // 2. 예매 상세 조회
  @Transactional(readOnly = true)
  public ReservationDetailResponse getDetailReservation(UUID reservationId, AuthInfo authInfo) {

    // 예매 id로 조회
    Reservation reservation =
        reservationRepository
            .findById(ReservationId.of(reservationId))
            .orElseThrow(
                () -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    validateReservationOwner(reservation, authInfo);

    return ReservationDetailResponse.from(reservation);
  }

  // 3. 예매 목록 조회
  @Transactional(readOnly = true)
  public Page<ReservationResponse> getAllReservations(
      UUID reserverId, Pageable pageable, AuthInfo authInfo) {

    return reservationRepository
        .findAllByCreatedBy(authInfo.userId(), pageable)
        .map(ReservationResponse::from);
  }

  // 4. 예매 취소
  @Transactional
  public void cancel(UUID reservationId, AuthInfo authInfo) {

    Reservation reservation =
        reservationRepository
            .findById(ReservationId.of(reservationId))
            .orElseThrow(
                () -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    validateReservationOwner(reservation, authInfo);

    // 예매 상태를 CANCEL로 변경
    reservation.cancel();

    // 예매 취소 로그 이벤트 발행
    try {
      ActorExtractor.ActorInfo actor = ActorExtractor.extract();

      reservationLogPort.publishAction(
          reservation.getId().toUuid(),
          reservation.getReservationNumber(),
          "CANCELED_BY_USER",
          actor.actorType(),
          actor.actorUserId(),
          LocalDateTime.now());
    } catch (Exception e) {
      log.warn("예매 취소 로그 발행 실패. reservationId={}", reservation.getId(), e);
    }

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
        reservationRepository.findAllByProductInfo_ProductId(productId, LocalDateTime.now());

    // 해당 예매가 없는 경우
    if (reservations.isEmpty()) {
      log.info("예매 없음. productId={}", productId);
      return;
    }

    for (Reservation reservation : reservations) {

      // CANCEL 상태로 변경
      reservation.cancelWithRefund();

      // 티켓 취소
      productCancelHelper.cancelTicketSafely(reservation.getId().toUuid());

      // 상품 취소로 인한 예매 취소 로그 이벤트 발행
      try {
        ActorExtractor.ActorInfo actor = ActorExtractor.extract();

        reservationLogPort.publishAction(
            reservation.getId().toUuid(),
            reservation.getReservationNumber(),
            "CANCELED_BY_PRODUCT",
            actor.actorType(),
            actor.actorUserId(),
            LocalDateTime.now());
      } catch (Exception e) {
        log.warn("상품 취소 예매 로그 발행 실패. reservationId={}", reservation.getId(), e);
      }
    }

    log.info("예매 취소 완료");

    // 3) 결제 환불 요청용 id 변환(string 변환)
    List<String> idsForRefund = reservations.stream().map(r -> r.getId().toString()).toList();

    // 4) 결제 환불 이벤트 발행
    eventPublisher.publishEvent(new ReservationCanceledEvent(idsForRefund, "PRODUCT_CANCEL"));
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
  public void expireReservations() {
    LocalDateTime now = LocalDateTime.now();

    // 현재를 기준으로 만료 예매 조회
    List<Reservation> targets = reservationRepository.findAllExpiredTargets(now);

    if (targets.isEmpty()) {
      return;
    }

    log.info("예매 만료 처리 시작. 대상 수={}", targets.size());

    int success = 0;
    int fail = 0;

    // 만료 상태로 변경 및 선점 취소
    for (Reservation reservation : targets) {

      boolean result = expireHelper.expireSafely(reservation.getId(), now);

      // 결과 true/false
      if (result) {
        success++;
        // 예매 만료 로그 이벤트 발행
        try {
          ActorExtractor.ActorInfo actor = ActorExtractor.extract();

          reservationLogPort.publishAction(
              reservation.getId().toUuid(),
              reservation.getReservationNumber(),
              "EXPIRED",
              actor.actorType(),
              actor.actorUserId(),
              now);
        } catch (Exception e) {
          log.warn("예매 만료 로그 발행 실패. reservationId={}", reservation.getId(), e);
        }
      } else {
        fail++;
      }
    }

    log.info("예매 만료 처리 완료. 성공={}, 실패={}", success, fail);
  }

  // 8. 결제 상태에 따라 예매 확정/취소 처리
  public void applyPaymentResult(String status, List<String> reservationIds) {

    // 결제 상태가 success인지 확인
    boolean isSuccess = "SUCCESS".equalsIgnoreCase(status);

    // 예매 id로 찾아서 예매 상태 변경
    for (String id : reservationIds) {
      ReservationId reservationId = ReservationId.of(UUID.fromString(id));

      // 예매 상태 변경
      paymentResultApplyHelper.applySafely(reservationId, isSuccess);

      // 결제 결과 로그 이벤트 발행
      try {
        ActorExtractor.ActorInfo actor = ActorExtractor.extract();

        reservationLogPort.publishAction(
            reservationId.toUuid(),
            null,
            isSuccess ? "CONFIRMED" : "PAYMENT_FAILED",
            actor.actorType(),
            actor.actorUserId(),
            LocalDateTime.now());
      } catch (Exception e) {
        log.warn("결제 결과 로그 발행 실패. reservationId={}, success={}", reservationId, isSuccess, e);
      }
    }
  }

  // 9. 예매 리스트 취소
  // 요청받은 예매 id 리스트를 돌면서, 예매 취소(상태 변경, 좌석 선점 취소), 티켓 취소, 결제 환불 api 호출
  @Transactional
  public void cancelReservations(List<UUID> reservationIds, AuthInfo authInfo) {

    // 1) 예매 조회
    List<ReservationId> ids = reservationIds.stream().map(ReservationId::of).toList();

    List<Reservation> reservations = reservationRepository.findAllByIdIn(ids);

    if (reservations.size() != reservationIds.size()) {
      throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }

    // 2) 예매 상태 변경 및 좌석 선점 취소, 티켓 취소
    for (Reservation reservation : reservations) {

      validateReservationOwner(reservation, authInfo);

      // CANCEL 상태로 변경
      reservation.cancelWithRefund();

      // 좌석 및 티켓 취소
      reservationCancelHelper.cancelExternalResources(reservation);

      // 예매 리스트 취소 로그 이벤트 발행
      try {
        ActorExtractor.ActorInfo actor = ActorExtractor.extract();

        reservationLogPort.publishAction(
            reservation.getId().toUuid(),
            reservation.getReservationNumber(),
            "CANCELED_BY_USER",
            actor.actorType(),
            actor.actorUserId(),
            LocalDateTime.now());
      } catch (Exception e) {
        log.warn("예매 리스트 취소 로그 발행 실패. reservationId={}", reservation.getId(), e);
      }
    }
    log.info("예매 취소 처리 완료");

    // 3) 결제 환불 요청용 id 변환(string 변환)
    List<String> idsForRefund = reservations.stream().map(r -> r.getId().toString()).toList();

    // 4) 결제 환불 이벤트 발행
    eventPublisher.publishEvent(new ReservationCanceledEvent(idsForRefund, "CUSTOMER_CANCEL"));
  }

  // =======================
  // 매서드 추출

  // 1. 예매 소유자 검증
  private void validateReservationOwner(Reservation reservation, AuthInfo authInfo) {
    if (authInfo.isAdmin()) {
      return;
    }
    if (!reservation.getCreatedBy().equals(authInfo.userId())) {
      throw new ReservationException(ReservationErrorCode.RESERVATION_OWNER_MISMATCH);
    }
  }
}
