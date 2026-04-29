package com.gavin.shipping.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        Long shipCount,
        Long crewCount,
        Long pendingTransportCount,
        Long inTransitCount,
        Long unsettledCount,
        BigDecimal totalReceivableAmount,
        BigDecimal totalReceivedAmount
) {
}
