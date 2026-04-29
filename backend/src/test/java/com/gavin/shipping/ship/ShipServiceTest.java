package com.gavin.shipping.ship;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.ShipStatus;
import com.gavin.shipping.ship.dto.CreateShipRequest;
import com.gavin.shipping.ship.dto.ShipCreateResponse;
import com.gavin.shipping.ship.dto.ShipResponse;
import com.gavin.shipping.ship.dto.UpdateShipRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class ShipServiceTest {

    @Mock
    private ShipMapper shipMapper;

    @InjectMocks
    private ShipService shipService;

    @Test
    void shouldReturnPagedShips() {
        when(shipMapper.count("远航", null, null, ShipStatus.IDLE)).thenReturn(1L);
        when(shipMapper.findPage("远航", null, null, ShipStatus.IDLE, 0, 10))
                .thenReturn(List.of(shipEntity()));

        PageResult<ShipResponse> result = shipService.findPage("远航", null, null, ShipStatus.IDLE, 1, 10);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).shipName()).isEqualTo("远航一号");
    }

    @Test
    void shouldCreateShipWhenShipNoIsUnique() {
        when(shipMapper.countByShipNo("SHIP-003")).thenReturn(0);
        doAnswer(invocation -> {
            ShipEntity ship = invocation.getArgument(0);
            ship.setId(3L);
            return 1;
        }).when(shipMapper).insert(any(ShipEntity.class));

        ShipCreateResponse response = shipService.create(createRequest());

        assertThat(response.id()).isEqualTo(3L);
        ArgumentCaptor<ShipEntity> captor = ArgumentCaptor.forClass(ShipEntity.class);
        verify(shipMapper).insert(captor.capture());
        assertThat(captor.getValue().getShipNo()).isEqualTo("SHIP-003");
        assertThat(captor.getValue().getStatus()).isEqualTo(ShipStatus.IDLE);
    }

    @Test
    void shouldRejectDuplicatedShipNo() {
        when(shipMapper.countByShipNo("SHIP-003")).thenReturn(1);

        assertThatThrownBy(() -> shipService.create(createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("船舶编号已存在");
        verify(shipMapper, never()).insert(any());
    }

    @Test
    void shouldUpdateExistingShip() {
        when(shipMapper.findById(1L)).thenReturn(Optional.of(shipEntity()));

        shipService.update(1L, new UpdateShipRequest(
                "远航一号",
                "散货船",
                new BigDecimal("52000.00"),
                "深圳港",
                ShipStatus.MAINTENANCE,
                "例行检修"
        ));

        ArgumentCaptor<ShipEntity> captor = ArgumentCaptor.forClass(ShipEntity.class);
        verify(shipMapper).update(captor.capture());
        assertThat(captor.getValue().getShipType()).isEqualTo("散货船");
        assertThat(captor.getValue().getStatus()).isEqualTo(ShipStatus.MAINTENANCE);
    }

    @Test
    void shouldRejectUpdateWhenShipNotFound() {
        when(shipMapper.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipService.update(99L, new UpdateShipRequest(
                "远航一号",
                "散货船",
                new BigDecimal("52000.00"),
                "深圳港",
                ShipStatus.MAINTENANCE,
                "例行检修"
        )))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("船舶不存在");
    }

    @Test
    void shouldRejectDeleteWhenShipHasActiveTransportOrders() {
        when(shipMapper.findById(1L)).thenReturn(Optional.of(shipEntity()));
        when(shipMapper.countActiveTransportOrders(1L)).thenReturn(1);

        assertThatThrownBy(() -> shipService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("正在执行运输任务的船舶不能删除");
        verify(shipMapper, never()).deleteById(1L);
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

    private ShipEntity shipEntity() {
        ShipEntity ship = new ShipEntity();
        ship.setId(1L);
        ship.setShipNo("SHIP-001");
        ship.setShipName("远航一号");
        ship.setShipType("集装箱船");
        ship.setLoadCapacity(new BigDecimal("50000.00"));
        ship.setHomePort("上海港");
        ship.setStatus(ShipStatus.IDLE);
        ship.setRemark("主力运输船舶");
        ship.setCreatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        ship.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        return ship;
    }
}
