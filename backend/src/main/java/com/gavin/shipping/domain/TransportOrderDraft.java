package com.gavin.shipping.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransportOrderDraft(
        String cargoName,
        BigDecimal cargoWeight,
        String originPort,
        String destinationPort,
        LocalDateTime plannedDepartureTime,
        LocalDateTime plannedArrivalTime
) {
}
