package com.tickatch.reservationservice.reservation.domain.repository;

import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.ReservationId;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, ReservationId> {

  List<Reservation> findAllByProductInfo_ProductId(Long productId);

  // 만료 대상 예매 조회
  @Query("""
          select r from Reservation r
          where r.status in ('INIT', 'PENDING_PAYMENT')
          and r.expireAt <= :now
      """)
  List<Reservation> findAllExpiredTargets(LocalDateTime now);
}
