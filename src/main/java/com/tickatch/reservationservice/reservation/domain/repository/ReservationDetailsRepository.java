package com.tickatch.reservationservice.reservation.domain.repository;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationDetailsRepository {

  //예매자 id와 상품 id로 조회
  Optional<Reservation> findByReserverIdAndProductId(UUID reserverId, Long productId);

  //예매자별 예매 전체 조회
  Page<Reservation> findAllByReserverId(UUID reserverId, Pageable pageable);
}
