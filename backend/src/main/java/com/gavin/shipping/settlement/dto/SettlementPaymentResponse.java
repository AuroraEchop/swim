package com.gavin.shipping.settlement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gavin.shipping.domain.SettlementStatus;

import java.time.LocalDateTime;

public record SettlementPaymentResponse(
        SettlementStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime settledAt
) {
}
