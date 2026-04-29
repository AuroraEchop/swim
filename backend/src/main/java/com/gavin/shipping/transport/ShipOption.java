package com.gavin.shipping.transport;

import com.gavin.shipping.domain.ShipStatus;

import java.math.BigDecimal;

public record ShipOption(
        Long id,
        String shipName,
        BigDecimal loadCapacity,
        ShipStatus status
) {
}
