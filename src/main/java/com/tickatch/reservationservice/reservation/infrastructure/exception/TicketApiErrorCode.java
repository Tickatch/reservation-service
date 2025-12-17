package com.tickatch.reservationservice.reservation.infrastructure.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TicketApiErrorCode implements ErrorCode {

  // 티켓 API 통신 실패
  TICKET_API_COMMUNICATION_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR.value(), "TICKET_API_COMMUNICATION_FAILED"),

  TICKET_CANCEL_FAILED(HttpStatus.BAD_REQUEST.value(), "TICKET_CANCEL_FAILED"),
  ;

  private final int status;
  private final String code;
}
