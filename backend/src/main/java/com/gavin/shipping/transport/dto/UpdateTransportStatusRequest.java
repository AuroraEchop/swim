package com.gavin.shipping.transport.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gavin.shipping.domain.TransportStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateTransportStatusRequest(
        @NotNull(message = "运输状态不能为空")
        TransportStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime actualDepartureTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime actualArrivalTime
) {
}
