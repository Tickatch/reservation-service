package com.tickatch.reservationservice.reservation.domain.repository;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, ReservationId> {

  List<Reservation> findAllByProductInfo_ProductId(Long productId);
}
