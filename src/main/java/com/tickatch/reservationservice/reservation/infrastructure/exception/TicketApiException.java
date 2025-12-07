package com.tickatch.reservationservice.reservation.infrastructure.exception;

import io.github.tickatch.common.error.BusinessException;
import io.github.tickatch.common.error.ErrorCode;

public class TicketApiException extends BusinessException {

  public TicketApiException(ErrorCode errorCode) {
    super(errorCode);
  }

  public TicketApiException(ErrorCode errorCode,
      Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public TicketApiException(ErrorCode errorCode, Throwable cause,
      Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}
