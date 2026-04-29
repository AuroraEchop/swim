package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.domain.SettlementResult;
import com.gavin.shipping.domain.SettlementStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementCalculatorTest {

    private final SettlementCalculator calculator = new SettlementCalculator();

    @Test
    void shouldCalculateUnsettledAmount() {
        SettlementResult result = calculator.calculate(
                new BigDecimal("20000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                BigDecimal.ZERO
        );

        assertThat(result.receivableAmount()).isEqualByComparingTo("20500.00");
        assertThat(result.status()).isEqualTo(SettlementStatus.UNSETTLED);
    }

    @Test
    void shouldCalculatePartialSettlement() {
        SettlementResult result = calculator.calculate(
                new BigDecimal("20000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("10000.00")
        );

        assertThat(result.receivableAmount()).isEqualByComparingTo("20500.00");
        assertThat(result.status()).isEqualTo(SettlementStatus.PARTIAL);
    }

    @Test
    void shouldCalculateCompletedSettlement() {
        SettlementResult result = calculator.calculate(
                new BigDecimal("20000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("20500.00")
        );

        assertThat(result.status()).isEqualTo(SettlementStatus.SETTLED);
    }

    @Test
    void shouldRejectReceivedAmountGreaterThanReceivableAmount() {
        assertThatThrownBy(() -> calculator.calculate(
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("101.00")
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("实收金额不能大于应收金额");
    }
}
