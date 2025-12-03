package com.tickatch.reservationservice.reservation.domain.exception;

import io.github.tickatch.common.error.BusinessException;
import io.github.tickatch.common.error.ErrorCode;

public class ReservationException extends BusinessException {

  public ReservationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ReservationException(ErrorCode errorCode,
      Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public ReservationException(ErrorCode errorCode, Throwable cause,
      Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}
