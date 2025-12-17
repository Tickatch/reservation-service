package com.tickatch.reservationservice.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reserver {

  @Column(length = 45, name = "reserverId")
  private UUID id;

  @Column(length = 45, name = "reserverName")
  private String name;

  protected Reserver(UUID id, String name) {
    this.id = id;
    this.name = name;
  }
}
