package com.tickatch.reservationservice.reservation.infrastructure.api;

import com.tickatch.reservationservice.reservation.domain.SeatAvailableCheck;
import org.springframework.stereotype.Service;

@Service
public class SeatAvailableCheckImpl implements SeatAvailableCheck {

  @Override
  public boolean check(long seatId) {

    // FeignClient와 같은 프레임워크의 기술을 이용해서 기능 구현
    return false;
  }
}
