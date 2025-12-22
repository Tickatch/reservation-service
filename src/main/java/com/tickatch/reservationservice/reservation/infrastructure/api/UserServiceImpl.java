package com.tickatch.reservationservice.reservation.infrastructure.api;

import com.tickatch.reservationservice.reservation.domain.dto.UserInformation;
import com.tickatch.reservationservice.reservation.domain.service.UserService;
import com.tickatch.reservationservice.reservation.infrastructure.client.UserFeignClient;
import com.tickatch.reservationservice.reservation.infrastructure.client.dto.UserClientResponse;
import io.github.tickatch.common.api.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserFeignClient userFeignClient;

  @Override
  public UserInformation getUser(UUID reserverId) {
    ApiResponse<UserClientResponse> response = userFeignClient.getRecipientInfo(reserverId);
    return response.getData().toUserInformation();
  }
}
