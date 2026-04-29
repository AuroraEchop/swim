package com.gavin.shipping.dashboard;

import com.gavin.shipping.dashboard.dto.DashboardSummaryResponse;
import com.gavin.shipping.dashboard.dto.RecentTransportOrderResponse;
import com.gavin.shipping.domain.TransportStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardMapper dashboardMapper;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void shouldReturnDashboardSummary() {
        when(dashboardMapper.countShips()).thenReturn(2L);
        when(dashboardMapper.countCrewMembers()).thenReturn(3L);
        when(dashboardMapper.countPendingTransportOrders()).thenReturn(1L);
        when(dashboardMapper.countInTransitTransportOrders()).thenReturn(0L);
        when(dashboardMapper.countUnsettledSettlements()).thenReturn(1L);
        when(dashboardMapper.sumReceivableAmount()).thenReturn(new BigDecimal("20500.00"));
        when(dashboardMapper.sumReceivedAmount()).thenReturn(BigDecimal.ZERO);

        DashboardSummaryResponse response = dashboardService.getSummary();

        assertThat(response.shipCount()).isEqualTo(2L);
        assertThat(response.crewCount()).isEqualTo(3L);
        assertThat(response.pendingTransportCount()).isEqualTo(1L);
        assertThat(response.totalReceivableAmount()).isEqualByComparingTo("20500.00");
    }

    @Test
    void shouldUseZeroWhenSettlementAmountIsNull() {
        DashboardSummaryResponse response = dashboardService.getSummary();

        assertThat(response.totalReceivableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.totalReceivedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldClampRecentTransportOrderLimit() {
        when(dashboardMapper.findRecentTransportOrders(20)).thenReturn(List.of(recentOrder()));

        List<RecentTransportOrderResponse> response = dashboardService.getRecentTransportOrders(99);

        assertThat(response).hasSize(1);
        verify(dashboardMapper).findRecentTransportOrders(20);
    }

    @Test
    void shouldUseMinimumLimit() {
        when(dashboardMapper.findRecentTransportOrders(1)).thenReturn(List.of(recentOrder()));

        List<RecentTransportOrderResponse> response = dashboardService.getRecentTransportOrders(0);

        assertThat(response).hasSize(1);
        verify(dashboardMapper).findRecentTransportOrders(1);
    }

    private RecentTransportOrderResponse recentOrder() {
        return new RecentTransportOrderResponse(
                1L,
                "TRANS-20260429-001",
                "电子设备",
                "远航一号",
                "上海港",
                "深圳港",
                TransportStatus.PENDING,
                LocalDateTime.of(2026, 5, 1, 8, 0)
        );
    }
}
