package com.gavin.shipping.ship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.ShipStatus;
import com.gavin.shipping.ship.dto.CreateShipRequest;
import com.gavin.shipping.ship.dto.ShipCreateResponse;
import com.gavin.shipping.ship.dto.ShipResponse;
import com.gavin.shipping.ship.dto.UpdateShipStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(ShipController.class)
class ShipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShipService shipService;

    @Test
    void shouldReturnPagedShips() throws Exception {
        when(shipService.findPage("远航", null, null, ShipStatus.IDLE, 1, 10))
                .thenReturn(PageResult.of(List.of(shipResponse()), 1, 10, 1));

        mockMvc.perform(get("/ships")
                        .param("keyword", "远航")
                        .param("status", "IDLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].shipNo").value("SHIP-001"))
                .andExpect(jsonPath("$.data.records[0].shipName").value("远航一号"));
    }

    @Test
    void shouldCreateShip() throws Exception {
        when(shipService.create(any(CreateShipRequest.class))).thenReturn(new ShipCreateResponse(3L));

        mockMvc.perform(post("/ships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void shouldRejectDuplicatedShipNo() throws Exception {
        when(shipService.create(any(CreateShipRequest.class))).thenThrow(new ConflictException("船舶编号已存在"));

        mockMvc.perform(post("/ships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("船舶编号已存在"));
    }

    @Test
    void shouldUpdateShipStatus() throws Exception {
        mockMvc.perform(patch("/ships/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateShipStatusRequest(ShipStatus.MAINTENANCE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("状态修改成功"));
    }

    @Test
    void shouldReturnNoContentWhenDeleteShip() throws Exception {
        mockMvc.perform(delete("/ships/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDeleteWhenShipHasActiveTransportOrders() throws Exception {
        doThrow(new ConflictException("正在执行运输任务的船舶不能删除")).when(shipService).delete(eq(1L));

        mockMvc.perform(delete("/ships/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("正在执行运输任务的船舶不能删除"));
    }

    private CreateShipRequest createRequest() {
        return new CreateShipRequest(
                "SHIP-003",
                "远航三号",
                "集装箱船",
                new BigDecimal("50000.00"),
                "上海港",
                ShipStatus.IDLE,
                "新增船舶"
        );
    }

    private ShipResponse shipResponse() {
        return new ShipResponse(
                1L,
                "SHIP-001",
                "远航一号",
                "集装箱船",
                new BigDecimal("50000.00"),
                "上海港",
                ShipStatus.IDLE,
                "主力运输船舶",
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 10, 0)
        );
    }
}
