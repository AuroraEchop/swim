package com.gavin.shipping.transport;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.SettlementStatus;
import com.gavin.shipping.domain.ShipStatus;
import com.gavin.shipping.domain.TransportOrderDraft;
import com.gavin.shipping.domain.TransportStatus;
import com.gavin.shipping.domain.Ship;
import com.gavin.shipping.service.TransportOrderValidator;
import com.gavin.shipping.service.TransportStatusService;
import com.gavin.shipping.transport.dto.CreateTransportOrderRequest;
import com.gavin.shipping.transport.dto.TransportOrderCreateResponse;
import com.gavin.shipping.transport.dto.TransportOrderResponse;
import com.gavin.shipping.transport.dto.UpdateTransportOrderRequest;
import com.gavin.shipping.transport.dto.UpdateTransportStatusRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TransportOrderService {

    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TransportOrderMapper transportOrderMapper;
    private final TransportOrderValidator transportOrderValidator;
    private final TransportStatusService transportStatusService;

    public TransportOrderService(TransportOrderMapper transportOrderMapper) {
        this.transportOrderMapper = transportOrderMapper;
        this.transportOrderValidator = new TransportOrderValidator();
        this.transportStatusService = new TransportStatusService();
    }

    public PageResult<TransportOrderResponse> findPage(
            String keyword,
            Long shipId,
            String originPort,
            String destinationPort,
            TransportStatus status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (safePage - 1) * safePageSize;
        long total = transportOrderMapper.count(keyword, shipId, originPort, destinationPort, status, startDate, endDate);
        List<TransportOrderResponse> records = transportOrderMapper
                .findPage(keyword, shipId, originPort, destinationPort, status, startDate, endDate, offset, safePageSize)
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResult.of(records, safePage, safePageSize, total);
    }

    public TransportOrderResponse findById(Long id) {
        return transportOrderMapper.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("运输任务不存在"));
    }

    public TransportOrderCreateResponse create(CreateTransportOrderRequest request) {
        ShipOption shipOption = transportOrderMapper.findShipById(request.shipId())
                .orElseThrow(() -> new NotFoundException("运输船舶不存在"));
        validateDraft(request, shipOption);
        ensureNoScheduleConflict(request.shipId(), request.plannedDepartureTime(), request.plannedArrivalTime(), null);

        TransportOrderEntity transportOrder = new TransportOrderEntity();
        transportOrder.setOrderNo(generateOrderNo());
        fillCreateFields(transportOrder, request);
        transportOrder.setStatus(TransportStatus.PENDING);
        transportOrderMapper.insert(transportOrder);
        return new TransportOrderCreateResponse(transportOrder.getId(), transportOrder.getOrderNo());
    }

    public void update(Long id, UpdateTransportOrderRequest request) {
        TransportOrderEntity existing = transportOrderMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("运输任务不存在"));
        if (existing.getStatus() == TransportStatus.ARRIVED || existing.getStatus() == TransportStatus.CANCELLED) {
            throw new ConflictException("已到达或已取消的运输任务不允许修改");
        }
        if (transportOrderMapper.countSettlementByTransportOrderId(id) > 0) {
            throw new ConflictException("已生成结算记录的运输任务不允许修改核心信息");
        }

        ShipOption shipOption = transportOrderMapper.findShipById(request.shipId())
                .orElseThrow(() -> new NotFoundException("运输船舶不存在"));
        validateDraft(request, shipOption);
        ensureNoScheduleConflict(request.shipId(), request.plannedDepartureTime(), request.plannedArrivalTime(), id);

        fillUpdateFields(existing, request);
        transportOrderMapper.update(existing);
    }

    public void updateStatus(Long id, UpdateTransportStatusRequest request) {
        TransportOrderEntity existing = transportOrderMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("运输任务不存在"));
        transportStatusService.validateTransition(existing.getStatus(), request.status());
        if (request.status() == TransportStatus.IN_TRANSIT && request.actualDepartureTime() == null) {
            throw new BusinessException("实际出发时间不能为空");
        }
        if (request.status() == TransportStatus.ARRIVED && request.actualArrivalTime() == null) {
            throw new BusinessException("实际到达时间不能为空");
        }

        existing.setStatus(request.status());
        if (request.status() == TransportStatus.IN_TRANSIT) {
            existing.setActualDepartureTime(request.actualDepartureTime());
        }
        if (request.status() == TransportStatus.ARRIVED) {
            existing.setActualArrivalTime(request.actualArrivalTime());
        }
        transportOrderMapper.updateStatus(existing);
    }

    public void delete(Long id) {
        TransportOrderEntity existing = transportOrderMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("运输任务不存在"));
        if (existing.getStatus() != TransportStatus.PENDING && existing.getStatus() != TransportStatus.CANCELLED) {
            throw new ConflictException("只有待出发或已取消的运输任务允许删除");
        }
        if (transportOrderMapper.countSettlementByTransportOrderId(id) > 0) {
            throw new ConflictException("已生成结算记录的运输任务不允许删除");
        }
        transportOrderMapper.deleteById(id);
    }

    private void validateDraft(CreateTransportOrderRequest request, ShipOption shipOption) {
        transportOrderValidator.validateCreate(
                new TransportOrderDraft(
                        request.cargoName(),
                        request.cargoWeight(),
                        request.originPort(),
                        request.destinationPort(),
                        request.plannedDepartureTime(),
                        request.plannedArrivalTime()
                ),
                toShip(shipOption)
        );
    }

    private void validateDraft(UpdateTransportOrderRequest request, ShipOption shipOption) {
        transportOrderValidator.validateCreate(
                new TransportOrderDraft(
                        request.cargoName(),
                        request.cargoWeight(),
                        request.originPort(),
                        request.destinationPort(),
                        request.plannedDepartureTime(),
                        request.plannedArrivalTime()
                ),
                toShip(shipOption)
        );
    }

    private Ship toShip(ShipOption shipOption) {
        return new Ship(shipOption.id(), null, shipOption.shipName(), shipOption.loadCapacity(), shipOption.status());
    }

    private void ensureNoScheduleConflict(
            Long shipId,
            LocalDateTime plannedDepartureTime,
            LocalDateTime plannedArrivalTime,
            Long excludeId
    ) {
        if (transportOrderMapper.countScheduleConflicts(shipId, plannedDepartureTime, plannedArrivalTime, excludeId) > 0) {
            throw new ConflictException("船舶已存在时间冲突的未完成运输任务");
        }
    }

    private String generateOrderNo() {
        String prefix = "TRANS-" + LocalDate.now().format(ORDER_DATE_FORMAT) + "-";
        int sequence = transportOrderMapper.countByOrderNoPrefix(prefix) + 1;
        return prefix + String.format("%03d", sequence);
    }

    private void fillCreateFields(TransportOrderEntity transportOrder, CreateTransportOrderRequest request) {
        transportOrder.setCargoName(request.cargoName());
        transportOrder.setCargoType(request.cargoType());
        transportOrder.setCargoWeight(request.cargoWeight());
        transportOrder.setOriginPort(request.originPort());
        transportOrder.setDestinationPort(request.destinationPort());
        transportOrder.setShipId(request.shipId());
        transportOrder.setCustomerName(request.customerName());
        transportOrder.setCustomerPhone(request.customerPhone());
        transportOrder.setPlannedDepartureTime(request.plannedDepartureTime());
        transportOrder.setPlannedArrivalTime(request.plannedArrivalTime());
        transportOrder.setRemark(request.remark());
    }

    private void fillUpdateFields(TransportOrderEntity transportOrder, UpdateTransportOrderRequest request) {
        transportOrder.setCargoName(request.cargoName());
        transportOrder.setCargoType(request.cargoType());
        transportOrder.setCargoWeight(request.cargoWeight());
        transportOrder.setOriginPort(request.originPort());
        transportOrder.setDestinationPort(request.destinationPort());
        transportOrder.setShipId(request.shipId());
        transportOrder.setCustomerName(request.customerName());
        transportOrder.setCustomerPhone(request.customerPhone());
        transportOrder.setPlannedDepartureTime(request.plannedDepartureTime());
        transportOrder.setPlannedArrivalTime(request.plannedArrivalTime());
        transportOrder.setRemark(request.remark());
    }

    private TransportOrderResponse toResponse(TransportOrderEntity transportOrder) {
        return new TransportOrderResponse(
                transportOrder.getId(),
                transportOrder.getOrderNo(),
                transportOrder.getCargoName(),
                transportOrder.getCargoType(),
                transportOrder.getCargoWeight(),
                transportOrder.getOriginPort(),
                transportOrder.getDestinationPort(),
                transportOrder.getShipId(),
                transportOrder.getShipName(),
                transportOrder.getCustomerName(),
                transportOrder.getCustomerPhone(),
                transportOrder.getPlannedDepartureTime(),
                transportOrder.getPlannedArrivalTime(),
                transportOrder.getActualDepartureTime(),
                transportOrder.getActualArrivalTime(),
                transportOrder.getStatus(),
                transportOrder.getSettlementId(),
                transportOrder.getSettlementStatus() == null ? SettlementStatus.UNSETTLED : transportOrder.getSettlementStatus(),
                transportOrder.getRemark(),
                transportOrder.getCreatedAt(),
                transportOrder.getUpdatedAt()
        );
    }
}
