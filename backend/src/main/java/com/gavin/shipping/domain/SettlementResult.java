package com.gavin.shipping.domain;

import java.math.BigDecimal;

public record SettlementResult(
        BigDecimal receivableAmount,
        SettlementStatus status
) {
}
