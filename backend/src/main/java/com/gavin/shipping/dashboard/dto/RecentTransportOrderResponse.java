package com.gavin.shipping.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gavin.shipping.domain.TransportStatus;

import java.time.LocalDateTime;

public record RecentTransportOrderResponse(
        Long id,
        String orderNo,
        String cargoName,
        String shipName,
        String originPort,
        String destinationPort,
        TransportStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime plannedDepartureTime
) {
}
