package com.tickatch.reservationservice.reservation.presentation.api;

import com.tickatch.reservationservice.reservation.application.service.ReservationService;
import com.tickatch.reservationservice.reservation.presentation.dto.ReservationDetailResponse;
import com.tickatch.reservationservice.reservation.presentation.dto.ReservationRequest;
import com.tickatch.reservationservice.reservation.presentation.dto.ReservationResponse;
import io.github.tickatch.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ApiResponse<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
    ReservationResponse response = reservationService.reserve(request);
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
}
