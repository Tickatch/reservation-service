package com.tickatch.reservationservice.reservation.infrastructure.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatApiErrorCode implements ErrorCode {

  // 좌석 API 통신 실패
  SEAT_API_COMMUNICATION_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR.value(), "SEAT_API_COMMUNICATION_FAILED"),

  // 좌석 선점 실패
  SEAT_PREEMPT_FAILED(HttpStatus.BAD_REQUEST.value(), "SEAT_API_PREEMPT_FAILED"),

  // 좌석 선점 해제 실패
  SEAT_PREEMPT_RELEASE_FAILED(HttpStatus.BAD_REQUEST.value(), "SEAT_API_PREEMPT_RELEASE_FAILED"),

  // 예약 확정 시 좌석 확정 API 응답 실패
  SEAT_CONFIRM_FAILED(HttpStatus.BAD_REQUEST.value(), "SEAT_API_CONFIRM_FAILED");

  private final int status;
  private final String code;
}
