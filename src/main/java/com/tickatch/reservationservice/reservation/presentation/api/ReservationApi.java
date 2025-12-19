package com.tickatch.reservationservice.reservation.presentation.api;

import com.tickatch.reservationservice.reservation.application.dto.request.PendingPaymentRequest;
import com.tickatch.reservationservice.reservation.application.dto.response.ReservationDetailResponse;
import com.tickatch.reservationservice.reservation.application.dto.response.ReservationResponse;
import com.tickatch.reservationservice.reservation.application.service.ReservationService;
import com.tickatch.reservationservice.reservation.presentation.dto.CreateReservationRequest;
import com.tickatch.reservationservice.reservation.presentation.dto.PaymentResultRequest;
import com.tickatch.reservationservice.reservation.presentation.dto.ReservationCancelRequest;
import io.github.tickatch.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationApi {

  private final ReservationService reservationService;

  // 1. 예매하기
  @PostMapping
  @Operation(summary = "새로운 예매 생성", description = "새로운 예매를 생성합니다.")
  public ApiResponse<ReservationResponse> reserve(
      @Valid @RequestBody CreateReservationRequest request) {
    ReservationResponse response = reservationService.reserve(request.toReservationRequest());
    return ApiResponse.success(response);
  }

  // 2. 예매 상세 조회
  @GetMapping("/{id}")
  @Operation(summary = "예매 상세 조회", description = "하나의 예매의 상세 정보를 조회합니다.")
  public ApiResponse<ReservationDetailResponse> getDetailReservation(@PathVariable UUID id) {
    ReservationDetailResponse response = reservationService.getDetailReservation(id);
    return ApiResponse.success(response);
  }

  // 3. 예매 목록 조회
  @GetMapping("/{reserverId}/list")
  @Operation(summary = "예매 목록 조회", description = "사용자가 예매한 목록 전체를 조회합니다.")
  public ApiResponse<Page<ReservationResponse>> getAllReservations(
      @PathVariable UUID reserverId, Pageable pageable) {
    Page<ReservationResponse> response =
        reservationService.getAllReservations(reserverId, pageable);
    return ApiResponse.success(response);
  }

  // 4. 예매 취소
  @PostMapping("/{id}/cancel")
  @Operation(summary = "예매 취소", description = "예매를 취소합니다.")
  public ApiResponse<Void> cancel(@PathVariable UUID id) {
    reservationService.cancel(id);
    return ApiResponse.success();
  }

  // 5. 예매 확정 상태 조회
  @GetMapping("/{id}/confirmed")
  @Operation(summary = "예매 확정 상태 조회", description = "티켓 생성 전 예매 확정 상태인지 조회합니다")
  public ApiResponse<Boolean> isConfirmed(@PathVariable UUID id) {
    boolean confirmed = reservationService.isConfirmed(id);
    return ApiResponse.success(confirmed);
  }

  // 6. 결제 상태 수신
  @PatchMapping("/payment-result")
  @Operation(summary = "결제 상태 수신", description = "결제 상태를 수신하여 예매를 확정/취소합니다.")
  public ApiResponse<Void> applyPaymentSResult(@RequestBody PaymentResultRequest request) {
    reservationService.applyPaymentResult(request.status(), request.reservationIds());
    return ApiResponse.success();
  }

  // 7. 예매 리스트 취소
  @PostMapping("/cancel")
  @Operation(summary = "예매 리스트 취소", description = "취소할 예매 리스트를 받고 예매 취소 및 결제와 연동하여 환불 처리한다.")
  public ApiResponse<Void> cancel(@RequestBody ReservationCancelRequest request) {
    reservationService.cancelReservations(request.toCancelRequest().reservationIds());
    return ApiResponse.success();
  }

  // 8. 예매 상태 변경용
  @PatchMapping("/pending-payment")
  @Operation(summary = "결제 진행중으로 상태 변경", description = "결제가 시작되면 예매 상태를 결제 진행중으로 변경합니다.")
  public ApiResponse<Void> markPendingPayment(@RequestBody PendingPaymentRequest request) {
    reservationService.markPendingPayment(request.toUUIDs());
    return ApiResponse.success();
  }
}
