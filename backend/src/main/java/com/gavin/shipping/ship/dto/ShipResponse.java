package com.gavin.shipping.ship.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gavin.shipping.domain.ShipStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipResponse(
        Long id,
        String shipNo,
        String shipName,
        String shipType,
        BigDecimal loadCapacity,
        String homePort,
        ShipStatus status,
        String remark,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}
