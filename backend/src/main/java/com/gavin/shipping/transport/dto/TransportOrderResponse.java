package com.gavin.shipping.transport.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gavin.shipping.domain.SettlementStatus;
import com.gavin.shipping.domain.TransportStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransportOrderResponse(
        Long id,
        String orderNo,
        String cargoName,
        String cargoType,
        BigDecimal cargoWeight,
        String originPort,
        String destinationPort,
        Long shipId,
        String shipName,
        String customerName,
        String customerPhone,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime plannedDepartureTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime plannedArrivalTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime actualDepartureTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime actualArrivalTime,

        TransportStatus status,
        Long settlementId,
        SettlementStatus settlementStatus,
        String remark,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}
