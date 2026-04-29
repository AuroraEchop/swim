package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.domain.TransportStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransportStatusServiceTest {

    private final TransportStatusService statusService = new TransportStatusService();

    @Test
    void shouldAllowPendingToInTransit() {
        assertThatCode(() -> statusService.validateTransition(TransportStatus.PENDING, TransportStatus.IN_TRANSIT))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowPendingToCancelled() {
        assertThatCode(() -> statusService.validateTransition(TransportStatus.PENDING, TransportStatus.CANCELLED))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowInTransitToArrived() {
        assertThatCode(() -> statusService.validateTransition(TransportStatus.IN_TRANSIT, TransportStatus.ARRIVED))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectTerminalStatusTransition() {
        assertThatThrownBy(() -> statusService.validateTransition(TransportStatus.ARRIVED, TransportStatus.CANCELLED))
                .isInstanceOf(BusinessException.class)
                .hasMessage("运输状态不允许这样流转");
    }
}
