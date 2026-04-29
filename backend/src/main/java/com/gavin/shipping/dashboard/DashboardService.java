package com.gavin.shipping.dashboard;

import com.gavin.shipping.dashboard.dto.DashboardSummaryResponse;
import com.gavin.shipping.dashboard.dto.RecentTransportOrderResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
                dashboardMapper.countShips(),
                dashboardMapper.countCrewMembers(),
                dashboardMapper.countPendingTransportOrders(),
                dashboardMapper.countInTransitTransportOrders(),
                dashboardMapper.countUnsettledSettlements(),
                defaultZero(dashboardMapper.sumReceivableAmount()),
                defaultZero(dashboardMapper.sumReceivedAmount())
        );
    }

    public List<RecentTransportOrderResponse> getRecentTransportOrders(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return dashboardMapper.findRecentTransportOrders(safeLimit);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
