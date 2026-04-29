package com.gavin.shipping.domain;

import java.math.BigDecimal;

public record Ship(
        Long id,
        String shipNo,
        String shipName,
        BigDecimal loadCapacity,
        ShipStatus status
) {
}
