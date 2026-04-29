package com.gavin.shipping.settlement;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.SettlementResult;
import com.gavin.shipping.domain.SettlementStatus;
import com.gavin.shipping.service.SettlementCalculator;
import com.gavin.shipping.settlement.dto.CreateSettlementRequest;
import com.gavin.shipping.settlement.dto.SettlementCreateResponse;
import com.gavin.shipping.settlement.dto.SettlementPaymentResponse;
import com.gavin.shipping.settlement.dto.SettlementResponse;
import com.gavin.shipping.settlement.dto.UpdateSettlementPaymentRequest;
import com.gavin.shipping.settlement.dto.UpdateSettlementRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SettlementService {

    private static final DateTimeFormatter SETTLEMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SettlementMapper settlementMapper;
    private final SettlementCalculator settlementCalculator;

    public SettlementService(SettlementMapper settlementMapper) {
        this.settlementMapper = settlementMapper;
        this.settlementCalculator = new SettlementCalculator();
    }

    public PageResult<SettlementResponse> findPage(
            String keyword,
            Long transportOrderId,
            String customerName,
            SettlementStatus status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (safePage - 1) * safePageSize;
        long total = settlementMapper.count(keyword, transportOrderId, customerName, status, startDate, endDate);
        List<SettlementResponse> records = settlementMapper
                .findPage(keyword, transportOrderId, customerName, status, startDate, endDate, offset, safePageSize)
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResult.of(records, safePage, safePageSize, total);
    }

    public SettlementResponse findById(Long id) {
        return settlementMapper.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("结算记录不存在"));
    }

    public SettlementCreateResponse create(CreateSettlementRequest request) {
        TransportOrderSummary transportOrder = settlementMapper.findTransportOrderById(request.transportOrderId())
                .orElseThrow(() -> new NotFoundException("运输任务不存在"));
        if (settlementMapper.countByTransportOrderId(request.transportOrderId()) > 0) {
            throw new ConflictException("该运输任务已生成结算记录");
        }

        SettlementEntity settlement = new SettlementEntity();
        settlement.setSettlementNo(generateSettlementNo());
        settlement.setTransportOrderId(transportOrder.id());
        settlement.setTransportOrderNo(transportOrder.orderNo());
        settlement.setCustomerName(transportOrder.customerName());
        fillAmounts(
                settlement,
                request.freightAmount(),
                request.additionalFee(),
                request.discountAmount(),
                request.receivedAmount(),
                null
        );
        settlement.setRemark(request.remark());
        settlementMapper.insert(settlement);
        return new SettlementCreateResponse(settlement.getId(), settlement.getSettlementNo());
    }

    public void update(Long id, UpdateSettlementRequest request) {
        SettlementEntity existing = settlementMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("结算记录不存在"));
        fillAmounts(
                existing,
                request.freightAmount(),
                request.additionalFee(),
                request.discountAmount(),
                request.receivedAmount(),
                null
        );
        existing.setRemark(request.remark());
        settlementMapper.update(existing);
    }

    public SettlementPaymentResponse updatePayment(Long id, UpdateSettlementPaymentRequest request) {
        SettlementEntity existing = settlementMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("结算记录不存在"));
        fillAmounts(
                existing,
                existing.getFreightAmount(),
                existing.getAdditionalFee(),
                existing.getDiscountAmount(),
                request.receivedAmount(),
                request.paymentTime()
        );
        settlementMapper.updatePayment(existing);
        return new SettlementPaymentResponse(existing.getStatus(), existing.getSettledAt());
    }

    public void delete(Long id) {
        SettlementEntity existing = settlementMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("结算记录不存在"));
        if (existing.getStatus() != SettlementStatus.UNSETTLED) {
            throw new ConflictException("只有未结算记录允许删除");
        }
        settlementMapper.deleteById(id);
    }

    private void fillAmounts(
            SettlementEntity settlement,
            BigDecimal freightAmount,
            BigDecimal additionalFee,
            BigDecimal discountAmount,
            BigDecimal receivedAmount,
            LocalDateTime paymentTime
    ) {
        SettlementResult result = settlementCalculator.calculate(
                freightAmount,
                additionalFee,
                discountAmount,
                receivedAmount
        );
        settlement.setFreightAmount(defaultZero(freightAmount));
        settlement.setAdditionalFee(defaultZero(additionalFee));
        settlement.setDiscountAmount(defaultZero(discountAmount));
        settlement.setReceivableAmount(result.receivableAmount());
        settlement.setReceivedAmount(defaultZero(receivedAmount));
        settlement.setStatus(result.status());
        settlement.setSettledAt(resolveSettledAt(result.status(), paymentTime));
    }

    private LocalDateTime resolveSettledAt(SettlementStatus status, LocalDateTime paymentTime) {
        if (status != SettlementStatus.SETTLED) {
            return null;
        }
        return paymentTime == null ? LocalDateTime.now() : paymentTime;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String generateSettlementNo() {
        String prefix = "SETTLE-" + LocalDate.now().format(SETTLEMENT_DATE_FORMAT) + "-";
        int sequence = settlementMapper.countBySettlementNoPrefix(prefix) + 1;
        return prefix + String.format("%03d", sequence);
    }

    private SettlementResponse toResponse(SettlementEntity settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getSettlementNo(),
                settlement.getTransportOrderId(),
                settlement.getTransportOrderNo(),
                settlement.getCustomerName(),
                settlement.getFreightAmount(),
                settlement.getAdditionalFee(),
                settlement.getDiscountAmount(),
                settlement.getReceivableAmount(),
                settlement.getReceivedAmount(),
                settlement.getStatus(),
                settlement.getSettledAt(),
                settlement.getRemark(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}
