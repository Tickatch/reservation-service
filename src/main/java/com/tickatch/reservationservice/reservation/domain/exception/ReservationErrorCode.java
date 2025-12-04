package com.tickatch.reservationservice.reservation.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
  RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "RESERVATION_NOT_FOUND"),

  INVALID_STATUS_FOR_PAYMENT(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS_FOR_PAYMENT"),
  ALREADY_CANCELED_OR_EXPIRED(HttpStatus.BAD_REQUEST.value(), "ALREADY_CANCELED_OR_EXPIRED"),
  INVALID_RESERVE_SEAT(HttpStatus.BAD_REQUEST.value(), "INVALID_RESERVE_SEAT"),

  SEAT_PREEMPT_FAILED(HttpStatus.BAD_REQUEST.value(), "SEAT_PREEMPT_FAILED"),
  RESERVATION_SAVE_FAILED(HttpStatus.BAD_REQUEST.value(), "RESERVATION_SAVE_FAILED"),
  SEAT_RESERVE_FAILED(HttpStatus.BAD_REQUEST.value(), "SEAT_RESERVE_FAILED"),
  ;

  private final int status;
  private final String code;
}
