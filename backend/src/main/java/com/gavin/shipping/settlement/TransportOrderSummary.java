package com.gavin.shipping.settlement;

public record TransportOrderSummary(
        Long id,
        String orderNo,
        String customerName
) {
}
