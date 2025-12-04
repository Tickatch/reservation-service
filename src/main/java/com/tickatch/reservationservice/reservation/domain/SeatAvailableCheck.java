package com.tickatch.reservationservice.reservation.domain;

public interface SeatAvailableCheck {

  boolean check(long seatId);
}
