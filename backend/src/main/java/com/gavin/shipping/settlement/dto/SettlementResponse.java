package com.gavin.shipping.settlement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gavin.shipping.domain.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        String settlementNo,
        Long transportOrderId,
        String transportOrderNo,
        String customerName,
        BigDecimal freightAmount,
        BigDecimal additionalFee,
        BigDecimal discountAmount,
        BigDecimal receivableAmount,
        BigDecimal receivedAmount,
        SettlementStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime settledAt,

        String remark,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}
