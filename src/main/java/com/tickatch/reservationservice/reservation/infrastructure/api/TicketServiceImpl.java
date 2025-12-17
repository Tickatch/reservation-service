package com.tickatch.reservationservice.reservation.infrastructure.api;

import com.tickatch.reservationservice.reservation.domain.service.TicketService;
import com.tickatch.reservationservice.reservation.infrastructure.client.TicketFeignClient;
import com.tickatch.reservationservice.reservation.infrastructure.exception.TicketApiErrorCode;
import com.tickatch.reservationservice.reservation.infrastructure.exception.TicketApiException;
import io.github.tickatch.common.api.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

  private final TicketFeignClient ticketFeignClient;

  @Override
  public void cancel(UUID reservationId) {
    try {
      ApiResponse<Void> response = ticketFeignClient.cancelTicketByReservation(reservationId);
      validate(response, TicketApiErrorCode.TICKET_CANCEL_FAILED);
    } catch (Exception e) {
      throw new TicketApiException(TicketApiErrorCode.TICKET_API_COMMUNICATION_FAILED);
    }
  }

  private void validate(ApiResponse<Void> response, TicketApiErrorCode errorCode) {
    if (!response.isSuccess()) {
      throw new TicketApiException(errorCode, response.getMessage());
    }
  }
}
