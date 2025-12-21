package com.tickatch.reservationservice.reservation.domain.service;

import com.tickatch.reservationservice.reservation.domain.dto.UserInformation;
import java.util.UUID;

public interface UserService {

  UserInformation getUser(UUID reserverId);
}
