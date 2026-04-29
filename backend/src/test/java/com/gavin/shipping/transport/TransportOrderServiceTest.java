package com.gavin.shipping.transport;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.SettlementStatus;
import com.gavin.shipping.domain.ShipStatus;
import com.gavin.shipping.domain.TransportStatus;
import com.gavin.shipping.transport.dto.CreateTransportOrderRequest;
import com.gavin.shipping.transport.dto.TransportOrderCreateResponse;
import com.gavin.shipping.transport.dto.TransportOrderResponse;
import com.gavin.shipping.transport.dto.UpdateTransportOrderRequest;
import com.gavin.shipping.transport.dto.UpdateTransportStatusRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportOrderServiceTest {

    @Mock
    private TransportOrderMapper transportOrderMapper;

    @InjectMocks
    private TransportOrderService transportOrderService;

    @Test
    void shouldReturnPagedTransportOrders() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);
        when(transportOrderMapper.count("电子", 1L, "上海港", "深圳港", TransportStatus.PENDING, startDate, endDate))
                .thenReturn(1L);
        when(transportOrderMapper.findPage("电子", 1L, "上海港", "深圳港", TransportStatus.PENDING, startDate, endDate, 0, 10))
                .thenReturn(List.of(orderEntity()));

        PageResult<TransportOrderResponse> result = transportOrderService.findPage(
                "电子", 1L, "上海港", "深圳港", TransportStatus.PENDING, startDate, endDate, 1, 10
        );

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).orderNo()).isEqualTo("TRANS-20260429-001");
        assertThat(result.records().get(0).settlementStatus()).isEqualTo(SettlementStatus.UNSETTLED);
    }

    @Test
    void shouldCreateTransportOrder() {
        when(transportOrderMapper.findShipById(1L)).thenReturn(Optional.of(shipOption()));
        when(transportOrderMapper.countScheduleConflicts(any(), any(), any(), any())).thenReturn(0);
        when(transportOrderMapper.countByOrderNoPrefix(any())).thenReturn(0);
        doAnswer(invocation -> {
            TransportOrderEntity order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        }).when(transportOrderMapper).insert(any(TransportOrderEntity.class));

        TransportOrderCreateResponse response = transportOrderService.create(createRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.orderNo()).startsWith("TRANS-");
        ArgumentCaptor<TransportOrderEntity> captor = ArgumentCaptor.forClass(TransportOrderEntity.class);
        verify(transportOrderMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransportStatus.PENDING);
        assertThat(captor.getValue().getCargoName()).isEqualTo("电子设备");
    }

    @Test
    void shouldRejectMissingShip() {
        when(transportOrderMapper.findShipById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transportOrderService.create(createRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("运输船舶不存在");
        verify(transportOrderMapper, never()).insert(any());
    }

    @Test
    void shouldRejectMaintenanceShip() {
        when(transportOrderMapper.findShipById(1L))
                .thenReturn(Optional.of(new ShipOption(1L, "远航一号", new BigDecimal("50000.00"), ShipStatus.MAINTENANCE)));

        assertThatThrownBy(() -> transportOrderService.create(createRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("维修中或停用的船舶不能创建运输任务");
    }

    @Test
    void shouldRejectScheduleConflict() {
        when(transportOrderMapper.findShipById(1L)).thenReturn(Optional.of(shipOption()));
        when(transportOrderMapper.countScheduleConflicts(any(), any(), any(), any())).thenReturn(1);

        assertThatThrownBy(() -> transportOrderService.create(createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("船舶已存在时间冲突的未完成运输任务");
        verify(transportOrderMapper, never()).insert(any());
    }

    @Test
    void shouldRejectUpdateWhenOrderArrived() {
        TransportOrderEntity existing = orderEntity();
        existing.setStatus(TransportStatus.ARRIVED);
        when(transportOrderMapper.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> transportOrderService.update(1L, updateRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("已到达或已取消的运输任务不允许修改");
    }

    @Test
    void shouldUpdateStatusFromPendingToInTransit() {
        when(transportOrderMapper.findById(1L)).thenReturn(Optional.of(orderEntity()));
        LocalDateTime actualDepartureTime = LocalDateTime.of(2026, 5, 1, 8, 10);

        transportOrderService.updateStatus(1L, new UpdateTransportStatusRequest(
                TransportStatus.IN_TRANSIT,
                actualDepartureTime,
                null
        ));

        ArgumentCaptor<TransportOrderEntity> captor = ArgumentCaptor.forClass(TransportOrderEntity.class);
        verify(transportOrderMapper).updateStatus(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransportStatus.IN_TRANSIT);
        assertThat(captor.getValue().getActualDepartureTime()).isEqualTo(actualDepartureTime);
    }

    @Test
    void shouldRejectInTransitWithoutActualDepartureTime() {
        when(transportOrderMapper.findById(1L)).thenReturn(Optional.of(orderEntity()));

        assertThatThrownBy(() -> transportOrderService.updateStatus(1L, new UpdateTransportStatusRequest(
                TransportStatus.IN_TRANSIT,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("实际出发时间不能为空");
    }

    @Test
    void shouldRejectDeleteWhenOrderInTransit() {
        TransportOrderEntity existing = orderEntity();
        existing.setStatus(TransportStatus.IN_TRANSIT);
        when(transportOrderMapper.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> transportOrderService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("只有待出发或已取消的运输任务允许删除");
        verify(transportOrderMapper, never()).deleteById(1L);
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

    private UpdateTransportOrderRequest updateRequest() {
        return new UpdateTransportOrderRequest(
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

    private ShipOption shipOption() {
        return new ShipOption(1L, "远航一号", new BigDecimal("50000.00"), ShipStatus.IDLE);
    }

    private TransportOrderEntity orderEntity() {
        TransportOrderEntity order = new TransportOrderEntity();
        order.setId(1L);
        order.setOrderNo("TRANS-20260429-001");
        order.setCargoName("电子设备");
        order.setCargoType("集装箱货物");
        order.setCargoWeight(new BigDecimal("1200.50"));
        order.setOriginPort("上海港");
        order.setDestinationPort("深圳港");
        order.setShipId(1L);
        order.setShipName("远航一号");
        order.setCustomerName("上海某贸易有限公司");
        order.setCustomerPhone("021-88888888");
        order.setPlannedDepartureTime(LocalDateTime.of(2026, 5, 1, 8, 0));
        order.setPlannedArrivalTime(LocalDateTime.of(2026, 5, 5, 18, 0));
        order.setStatus(TransportStatus.PENDING);
        order.setSettlementStatus(SettlementStatus.UNSETTLED);
        order.setRemark("注意防潮");
        order.setCreatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        order.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        return order;
    }
}
