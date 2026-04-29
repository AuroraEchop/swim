package com.gavin.shipping.settlement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateSettlementPaymentRequest(
        @NotNull(message = "实收金额不能为空")
        @DecimalMin(value = "0.00", message = "实收金额不能小于0")
        BigDecimal receivedAmount,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime paymentTime
) {
}
