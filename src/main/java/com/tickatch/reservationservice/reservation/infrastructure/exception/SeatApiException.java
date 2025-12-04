package com.tickatch.reservationservice.reservation.infrastructure.exception;

import io.github.tickatch.common.error.BusinessException;
import io.github.tickatch.common.error.ErrorCode;

public class SeatApiException extends BusinessException {

  public SeatApiException(ErrorCode errorCode) {
    super(errorCode);
  }

  public SeatApiException(ErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public SeatApiException(ErrorCode errorCode, Throwable cause, Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}
