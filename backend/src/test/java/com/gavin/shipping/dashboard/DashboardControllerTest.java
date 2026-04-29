package com.gavin.shipping.dashboard;

import com.gavin.shipping.dashboard.dto.DashboardSummaryResponse;
import com.gavin.shipping.dashboard.dto.RecentTransportOrderResponse;
import com.gavin.shipping.domain.TransportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void shouldReturnDashboardSummary() throws Exception {
        when(dashboardService.getSummary()).thenReturn(new DashboardSummaryResponse(
                2L,
                3L,
                1L,
                0L,
                1L,
                new BigDecimal("20500.00"),
                BigDecimal.ZERO
        ));

        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.shipCount").value(2))
                .andExpect(jsonPath("$.data.crewCount").value(3))
                .andExpect(jsonPath("$.data.totalReceivableAmount").value(20500.00));
    }

    @Test
    void shouldReturnRecentTransportOrders() throws Exception {
        when(dashboardService.getRecentTransportOrders(5)).thenReturn(List.of(recentOrder()));

        mockMvc.perform(get("/dashboard/recent-transport-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].orderNo").value("TRANS-20260429-001"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].plannedDepartureTime").value("2026-05-01 08:00:00"));
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
