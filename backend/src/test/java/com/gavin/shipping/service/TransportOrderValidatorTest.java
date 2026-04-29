package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.domain.Ship;
import com.gavin.shipping.domain.ShipStatus;
import com.gavin.shipping.domain.TransportOrderDraft;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransportOrderValidatorTest {

    private final TransportOrderValidator validator = new TransportOrderValidator();

    @Test
    void shouldAcceptValidTransportOrder() {
        assertThatCode(() -> validator.validateCreate(validDraft(), validShip()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSameOriginAndDestinationPort() {
        TransportOrderDraft draft = new TransportOrderDraft(
                "电子设备",
                new BigDecimal("1200.50"),
                "上海港",
                "上海港",
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 5, 18, 0)
        );

        assertThatThrownBy(() -> validator.validateCreate(draft, validShip()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("起运港和目的港不能相同");
    }

    @Test
    void shouldRejectArrivalBeforeDeparture() {
        TransportOrderDraft draft = new TransportOrderDraft(
                "电子设备",
                new BigDecimal("1200.50"),
                "上海港",
                "深圳港",
                LocalDateTime.of(2026, 5, 5, 18, 0),
                LocalDateTime.of(2026, 5, 1, 8, 0)
        );

        assertThatThrownBy(() -> validator.validateCreate(draft, validShip()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("预计到达时间必须晚于预计出发时间");
    }

    @Test
    void shouldRejectMaintenanceShip() {
        Ship ship = new Ship(1L, "SHIP-001", "远航一号", new BigDecimal("50000.00"), ShipStatus.MAINTENANCE);

        assertThatThrownBy(() -> validator.validateCreate(validDraft(), ship))
                .isInstanceOf(BusinessException.class)
                .hasMessage("维修中或停用的船舶不能创建运输任务");
    }

    @Test
    void shouldRejectOverloadedCargo() {
        Ship ship = new Ship(1L, "SHIP-001", "远航一号", new BigDecimal("100.00"), ShipStatus.IDLE);

        assertThatThrownBy(() -> validator.validateCreate(validDraft(), ship))
                .isInstanceOf(BusinessException.class)
                .hasMessage("货物重量不能超过船舶载重量");
    }

    private TransportOrderDraft validDraft() {
        return new TransportOrderDraft(
                "电子设备",
                new BigDecimal("1200.50"),
                "上海港",
                "深圳港",
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 5, 18, 0)
        );
    }

    private Ship validShip() {
        return new Ship(1L, "SHIP-001", "远航一号", new BigDecimal("50000.00"), ShipStatus.IDLE);
    }
}
