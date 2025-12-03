package com.tickatch.reservationservice.reservation.domain;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationId {

  private UUID id;

  private ReservationId(UUID id) {
    this.id = id;
  }

  public static ReservationId generateId() {
    return new ReservationId(UUID.randomUUID());
  }

  public static ReservationId of(UUID id) {
    return new ReservationId(id);
  }

  public UUID toUuid() {
    return id;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
