package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.domain.Ship;
import com.gavin.shipping.domain.ShipStatus;
import com.gavin.shipping.domain.TransportOrderDraft;

import java.math.BigDecimal;

public class TransportOrderValidator {

    public void validateCreate(TransportOrderDraft draft, Ship ship) {
        if (draft == null) {
            throw new BusinessException("运输任务不能为空");
        }
        if (ship == null) {
            throw new BusinessException("运输船舶不能为空");
        }
        if (isBlank(draft.cargoName())) {
            throw new BusinessException("货物名称不能为空");
        }
        if (draft.cargoWeight() == null || draft.cargoWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("货物重量必须大于0");
        }
        if (isBlank(draft.originPort()) || isBlank(draft.destinationPort())) {
            throw new BusinessException("起运港和目的港不能为空");
        }
        if (draft.originPort().equals(draft.destinationPort())) {
            throw new BusinessException("起运港和目的港不能相同");
        }
        if (draft.plannedDepartureTime() == null || draft.plannedArrivalTime() == null) {
            throw new BusinessException("预计运输时间不能为空");
        }
        if (!draft.plannedArrivalTime().isAfter(draft.plannedDepartureTime())) {
            throw new BusinessException("预计到达时间必须晚于预计出发时间");
        }
        if (ship.status() == ShipStatus.MAINTENANCE || ship.status() == ShipStatus.DISABLED) {
            throw new BusinessException("维修中或停用的船舶不能创建运输任务");
        }
        if (ship.loadCapacity().compareTo(draft.cargoWeight()) < 0) {
            throw new BusinessException("货物重量不能超过船舶载重量");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
