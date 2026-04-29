package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.domain.SettlementResult;
import com.gavin.shipping.domain.SettlementStatus;

import java.math.BigDecimal;

public class SettlementCalculator {

    public SettlementResult calculate(
            BigDecimal freightAmount,
            BigDecimal additionalFee,
            BigDecimal discountAmount,
            BigDecimal receivedAmount
    ) {
        BigDecimal freight = defaultZero(freightAmount);
        BigDecimal additional = defaultZero(additionalFee);
        BigDecimal discount = defaultZero(discountAmount);
        BigDecimal received = defaultZero(receivedAmount);

        requireNonNegative(freight, "运费金额不能小于0");
        requireNonNegative(additional, "附加费用不能小于0");
        requireNonNegative(discount, "优惠金额不能小于0");
        requireNonNegative(received, "实收金额不能小于0");

        BigDecimal receivable = freight.add(additional).subtract(discount);
        if (receivable.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("应收金额不能小于0");
        }
        if (received.compareTo(receivable) > 0) {
            throw new BusinessException("实收金额不能大于应收金额");
        }

        return new SettlementResult(receivable, resolveStatus(receivable, received));
    }

    private SettlementStatus resolveStatus(BigDecimal receivable, BigDecimal received) {
        if (received.compareTo(BigDecimal.ZERO) == 0) {
            return SettlementStatus.UNSETTLED;
        }
        if (received.compareTo(receivable) < 0) {
            return SettlementStatus.PARTIAL;
        }
        return SettlementStatus.SETTLED;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void requireNonNegative(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(message);
        }
    }
}
