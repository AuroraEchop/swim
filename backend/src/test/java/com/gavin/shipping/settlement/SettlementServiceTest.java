package com.gavin.shipping.settlement;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.SettlementStatus;
import com.gavin.shipping.settlement.dto.CreateSettlementRequest;
import com.gavin.shipping.settlement.dto.SettlementCreateResponse;
import com.gavin.shipping.settlement.dto.SettlementPaymentResponse;
import com.gavin.shipping.settlement.dto.SettlementResponse;
import com.gavin.shipping.settlement.dto.UpdateSettlementPaymentRequest;
import com.gavin.shipping.settlement.dto.UpdateSettlementRequest;
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
class SettlementServiceTest {

    @Mock
    private SettlementMapper settlementMapper;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    void shouldReturnPagedSettlements() {
        LocalDate startDate = LocalDate.of(2026, 4, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 30);
        when(settlementMapper.count("SETTLE", 1L, "上海", SettlementStatus.UNSETTLED, startDate, endDate))
                .thenReturn(1L);
        when(settlementMapper.findPage("SETTLE", 1L, "上海", SettlementStatus.UNSETTLED, startDate, endDate, 0, 10))
                .thenReturn(List.of(settlementEntity()));

        PageResult<SettlementResponse> result = settlementService.findPage(
                "SETTLE", 1L, "上海", SettlementStatus.UNSETTLED, startDate, endDate, 1, 10
        );

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).settlementNo()).isEqualTo("SETTLE-20260429-001");
        assertThat(result.records().get(0).receivableAmount()).isEqualByComparingTo("20500.00");
    }

    @Test
    void shouldCreateSettlement() {
        when(settlementMapper.findTransportOrderById(1L))
                .thenReturn(Optional.of(new TransportOrderSummary(1L, "TRANS-20260429-001", "上海某贸易有限公司")));
        when(settlementMapper.countByTransportOrderId(1L)).thenReturn(0);
        when(settlementMapper.countBySettlementNoPrefix(any())).thenReturn(0);
        doAnswer(invocation -> {
            SettlementEntity settlement = invocation.getArgument(0);
            settlement.setId(1L);
            return 1;
        }).when(settlementMapper).insert(any(SettlementEntity.class));

        SettlementCreateResponse response = settlementService.create(createRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.settlementNo()).startsWith("SETTLE-");
        ArgumentCaptor<SettlementEntity> captor = ArgumentCaptor.forClass(SettlementEntity.class);
        verify(settlementMapper).insert(captor.capture());
        assertThat(captor.getValue().getCustomerName()).isEqualTo("上海某贸易有限公司");
        assertThat(captor.getValue().getReceivableAmount()).isEqualByComparingTo("20500.00");
        assertThat(captor.getValue().getStatus()).isEqualTo(SettlementStatus.UNSETTLED);
    }

    @Test
    void shouldRejectMissingTransportOrder() {
        when(settlementMapper.findTransportOrderById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> settlementService.create(createRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("运输任务不存在");
        verify(settlementMapper, never()).insert(any());
    }

    @Test
    void shouldRejectDuplicateSettlementForTransportOrder() {
        when(settlementMapper.findTransportOrderById(1L))
                .thenReturn(Optional.of(new TransportOrderSummary(1L, "TRANS-20260429-001", "上海某贸易有限公司")));
        when(settlementMapper.countByTransportOrderId(1L)).thenReturn(1);

        assertThatThrownBy(() -> settlementService.create(createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("该运输任务已生成结算记录");
        verify(settlementMapper, never()).insert(any());
    }

    @Test
    void shouldUpdateSettlementAndRecalculateStatus() {
        when(settlementMapper.findById(1L)).thenReturn(Optional.of(settlementEntity()));

        settlementService.update(1L, new UpdateSettlementRequest(
                new BigDecimal("20000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("10000.00"),
                "部分回款"
        ));

        ArgumentCaptor<SettlementEntity> captor = ArgumentCaptor.forClass(SettlementEntity.class);
        verify(settlementMapper).update(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SettlementStatus.PARTIAL);
        assertThat(captor.getValue().getSettledAt()).isNull();
        assertThat(captor.getValue().getRemark()).isEqualTo("部分回款");
    }

    @Test
    void shouldUpdatePaymentAndMarkSettled() {
        SettlementEntity existing = settlementEntity();
        when(settlementMapper.findById(1L)).thenReturn(Optional.of(existing));
        LocalDateTime paymentTime = LocalDateTime.of(2026, 4, 29, 15, 30);

        SettlementPaymentResponse response = settlementService.updatePayment(
                1L,
                new UpdateSettlementPaymentRequest(new BigDecimal("20500.00"), paymentTime)
        );

        assertThat(response.status()).isEqualTo(SettlementStatus.SETTLED);
        assertThat(response.settledAt()).isEqualTo(paymentTime);
        ArgumentCaptor<SettlementEntity> captor = ArgumentCaptor.forClass(SettlementEntity.class);
        verify(settlementMapper).updatePayment(captor.capture());
        assertThat(captor.getValue().getReceivedAmount()).isEqualByComparingTo("20500.00");
    }

    @Test
    void shouldRejectReceivedAmountGreaterThanReceivable() {
        when(settlementMapper.findById(1L)).thenReturn(Optional.of(settlementEntity()));

        assertThatThrownBy(() -> settlementService.updatePayment(
                1L,
                new UpdateSettlementPaymentRequest(new BigDecimal("30000.00"), null)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("实收金额不能大于应收金额");
        verify(settlementMapper, never()).updatePayment(any());
    }

    @Test
    void shouldRejectDeleteWhenSettlementIsPartial() {
        SettlementEntity existing = settlementEntity();
        existing.setStatus(SettlementStatus.PARTIAL);
        when(settlementMapper.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> settlementService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("只有未结算记录允许删除");
        verify(settlementMapper, never()).deleteById(1L);
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

    private SettlementEntity settlementEntity() {
        SettlementEntity settlement = new SettlementEntity();
        settlement.setId(1L);
        settlement.setSettlementNo("SETTLE-20260429-001");
        settlement.setTransportOrderId(1L);
        settlement.setTransportOrderNo("TRANS-20260429-001");
        settlement.setCustomerName("上海某贸易有限公司");
        settlement.setFreightAmount(new BigDecimal("20000.00"));
        settlement.setAdditionalFee(new BigDecimal("1000.00"));
        settlement.setDiscountAmount(new BigDecimal("500.00"));
        settlement.setReceivableAmount(new BigDecimal("20500.00"));
        settlement.setReceivedAmount(BigDecimal.ZERO);
        settlement.setStatus(SettlementStatus.UNSETTLED);
        settlement.setRemark("接口验证结算");
        settlement.setCreatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        settlement.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        return settlement;
    }
}
