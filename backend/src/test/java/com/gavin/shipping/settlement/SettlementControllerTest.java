package com.gavin.shipping.settlement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.SettlementStatus;
import com.gavin.shipping.settlement.dto.CreateSettlementRequest;
import com.gavin.shipping.settlement.dto.SettlementCreateResponse;
import com.gavin.shipping.settlement.dto.SettlementPaymentResponse;
import com.gavin.shipping.settlement.dto.SettlementResponse;
import com.gavin.shipping.settlement.dto.UpdateSettlementPaymentRequest;
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

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SettlementService settlementService;

    @Test
    void shouldReturnPagedSettlements() throws Exception {
        when(settlementService.findPage(
                "SETTLE", 1L, "上海", SettlementStatus.UNSETTLED,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), 1, 10
        )).thenReturn(PageResult.of(List.of(settlementResponse()), 1, 10, 1));

        mockMvc.perform(get("/settlements")
                        .param("keyword", "SETTLE")
                        .param("transportOrderId", "1")
                        .param("customerName", "上海")
                        .param("status", "UNSETTLED")
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].settlementNo").value("SETTLE-20260429-001"))
                .andExpect(jsonPath("$.data.records[0].status").value("UNSETTLED"));
    }

    @Test
    void shouldCreateSettlement() throws Exception {
        when(settlementService.create(any(CreateSettlementRequest.class)))
                .thenReturn(new SettlementCreateResponse(1L, "SETTLE-20260429-001"));

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.settlementNo").value("SETTLE-20260429-001"));
    }

    @Test
    void shouldRejectDuplicateSettlement() throws Exception {
        when(settlementService.create(any(CreateSettlementRequest.class)))
                .thenThrow(new ConflictException("该运输任务已生成结算记录"));

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该运输任务已生成结算记录"));
    }

    @Test
    void shouldUpdatePayment() throws Exception {
        LocalDateTime paymentTime = LocalDateTime.of(2026, 4, 29, 15, 30);
        when(settlementService.updatePayment(eq(1L), any(UpdateSettlementPaymentRequest.class)))
                .thenReturn(new SettlementPaymentResponse(SettlementStatus.SETTLED, paymentTime));

        mockMvc.perform(patch("/settlements/{id}/payment", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateSettlementPaymentRequest(
                                new BigDecimal("20500.00"),
                                paymentTime
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SETTLED"))
                .andExpect(jsonPath("$.data.settledAt").value("2026-04-29 15:30:00"));
    }

    @Test
    void shouldReturnNoContentWhenDeleteSettlement() throws Exception {
        mockMvc.perform(delete("/settlements/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDeleteWhenSettlementCannotBeDeleted() throws Exception {
        doThrow(new ConflictException("只有未结算记录允许删除"))
                .when(settlementService).delete(eq(1L));

        mockMvc.perform(delete("/settlements/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("只有未结算记录允许删除"));
    }

    private CreateSettlementRequest createRequest() {
        return new CreateSettlementRequest(
                1L,
                new BigDecimal("20000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                "接口验证结算"
        );
    }

    private SettlementResponse settlementResponse() {
        return new SettlementResponse(
                1L,
                "SETTLE-20260429-001",
                1L,
                "TRANS-20260429-001",
                "上海某贸易有限公司",
                new BigDecimal("20000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("20500.00"),
                BigDecimal.ZERO,
                SettlementStatus.UNSETTLED,
                null,
                "接口验证结算",
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 10, 0)
        );
    }
}
