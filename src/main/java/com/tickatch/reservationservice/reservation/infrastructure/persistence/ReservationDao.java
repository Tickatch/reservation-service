package com.tickatch.reservationservice.reservation.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tickatch.reservationservice.reservation.domain.QReservation;
import com.tickatch.reservationservice.reservation.domain.Reservation;
import com.tickatch.reservationservice.reservation.domain.repository.ReservationDetailsRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationDao implements ReservationDetailsRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<Reservation> findByReserverIdAndProductId(UUID reserverId, Long productId) {
    return Optional.empty();
  }

  @Override
  public Page<Reservation> findAllByReserverId(UUID reserverId, Pageable pageable) {

    QReservation reservation = QReservation.reservation;

    var content =
        queryFactory
            .selectFrom(reservation)
            .where(reservation.reserver.id.eq(reserverId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(reservation.createdAt.desc())
            .fetch();

    long total =
        queryFactory
            .select(reservation.count())
            .from(reservation)
            .where(reservation.reserver.id.eq(reserverId))
            .fetchOne();

    return new PageImpl<>(content, pageable, total);
  }
}
