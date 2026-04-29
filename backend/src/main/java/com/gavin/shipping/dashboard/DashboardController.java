package com.gavin.shipping.dashboard;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.dashboard.dto.DashboardSummaryResponse;
import com.gavin.shipping.dashboard.dto.RecentTransportOrderResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    @GetMapping("/recent-transport-orders")
    public ApiResponse<List<RecentTransportOrderResponse>> getRecentTransportOrders(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.success(dashboardService.getRecentTransportOrders(limit));
    }
}
