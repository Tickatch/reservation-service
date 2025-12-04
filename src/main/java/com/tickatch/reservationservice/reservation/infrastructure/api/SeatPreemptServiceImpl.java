package com.tickatch.reservationservice.reservation.infrastructure.api;

import com.tickatch.reservationservice.reservation.domain.service.SeatPreemptService;
import com.tickatch.reservationservice.reservation.infrastructure.client.SeatFeignClient;
import com.tickatch.reservationservice.reservation.infrastructure.exception.SeatApiErrorCode;
import com.tickatch.reservationservice.reservation.infrastructure.exception.SeatApiException;
import io.github.tickatch.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatPreemptServiceImpl implements SeatPreemptService {

  private final SeatFeignClient seatFeignClient;

  @Override
  public void reserve(long seatId) {
    try {
      ApiResponse<Void> response = seatFeignClient.reserve(seatId);
      validate(response, SeatApiErrorCode.SEAT_CONFIRM_FAILED);
    } catch (Exception e) {
      throw new SeatApiException(SeatApiErrorCode.SEAT_API_COMMUNICATION_FAILED);
    }
  }

  @Override
  public void preempt(long seatId) {
    try {
      ApiResponse<Void> response = seatFeignClient.preempt(seatId);
      validate(response, SeatApiErrorCode.SEAT_PREEMPT_FAILED);
    } catch (Exception e) {
      throw new SeatApiException(SeatApiErrorCode.SEAT_API_COMMUNICATION_FAILED);
    }
  }

  @Override
  public void cancel(long seatId) {
    try {
      ApiResponse<Void> response = seatFeignClient.cancel(seatId);
      validate(response, SeatApiErrorCode.SEAT_PREEMPT_RELEASE_FAILED);
    } catch (Exception e) {
      throw new SeatApiException(SeatApiErrorCode.SEAT_API_COMMUNICATION_FAILED);
    }
  }

  private void validate(ApiResponse<Void> response, SeatApiErrorCode errorCode) {
    if (!response.isSuccess()) {
      throw new SeatApiException(errorCode, response.getMessage());
    }
  }
}
