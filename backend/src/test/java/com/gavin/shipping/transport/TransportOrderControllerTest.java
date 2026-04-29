package com.gavin.shipping.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.SettlementStatus;
import com.gavin.shipping.domain.TransportStatus;
import com.gavin.shipping.transport.dto.CreateTransportOrderRequest;
import com.gavin.shipping.transport.dto.TransportOrderCreateResponse;
import com.gavin.shipping.transport.dto.TransportOrderResponse;
import com.gavin.shipping.transport.dto.UpdateTransportStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransportOrderController.class)
class TransportOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransportOrderService transportOrderService;

    @Test
    void shouldReturnPagedTransportOrders() throws Exception {
        when(transportOrderService.findPage(
                "电子", 1L, "上海港", "深圳港", TransportStatus.PENDING,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), 1, 10
        )).thenReturn(PageResult.of(List.of(orderResponse()), 1, 10, 1));

        mockMvc.perform(get("/transport-orders")
                        .param("keyword", "电子")
                        .param("shipId", "1")
                        .param("originPort", "上海港")
                        .param("destinationPort", "深圳港")
                        .param("status", "PENDING")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("TRANS-20260429-001"))
                .andExpect(jsonPath("$.data.records[0].settlementStatus").value("UNSETTLED"));
    }

    @Test
    void shouldCreateTransportOrder() throws Exception {
        when(transportOrderService.create(any(CreateTransportOrderRequest.class)))
                .thenReturn(new TransportOrderCreateResponse(1L, "TRANS-20260429-001"));

        mockMvc.perform(post("/transport-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("TRANS-20260429-001"));
    }

    @Test
    void shouldRejectScheduleConflict() throws Exception {
        when(transportOrderService.create(any(CreateTransportOrderRequest.class)))
                .thenThrow(new ConflictException("船舶已存在时间冲突的未完成运输任务"));

        mockMvc.perform(post("/transport-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("船舶已存在时间冲突的未完成运输任务"));
    }

    @Test
    void shouldUpdateTransportStatus() throws Exception {
        mockMvc.perform(patch("/transport-orders/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTransportStatusRequest(
                                TransportStatus.IN_TRANSIT,
                                LocalDateTime.of(2026, 5, 1, 8, 10),
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("状态修改成功"));
    }

    @Test
    void shouldReturnNoContentWhenDeleteTransportOrder() throws Exception {
        mockMvc.perform(delete("/transport-orders/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDeleteWhenTransportOrderCannotBeDeleted() throws Exception {
        doThrow(new ConflictException("只有待出发或已取消的运输任务允许删除"))
                .when(transportOrderService).delete(eq(1L));

        mockMvc.perform(delete("/transport-orders/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("只有待出发或已取消的运输任务允许删除"));
    }

    private CreateTransportOrderRequest createRequest() {
        return new CreateTransportOrderRequest(
                "电子设备",
                "集装箱货物",
                new BigDecimal("1200.50"),
                "上海港",
                "深圳港",
                1L,
                "上海某贸易有限公司",
                "021-88888888",
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 5, 18, 0),
                "注意防潮"
        );
    }

    private TransportOrderResponse orderResponse() {
        return new TransportOrderResponse(
                1L,
                "TRANS-20260429-001",
                "电子设备",
                "集装箱货物",
                new BigDecimal("1200.50"),
                "上海港",
                "深圳港",
                1L,
                "远航一号",
                "上海某贸易有限公司",
                "021-88888888",
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 5, 18, 0),
                null,
                null,
                TransportStatus.PENDING,
                null,
                SettlementStatus.UNSETTLED,
                "注意防潮",
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 10, 0)
        );
    }
}
