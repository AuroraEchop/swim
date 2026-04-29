package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.domain.TransportStatus;

public class TransportStatusService {

    public void validateTransition(TransportStatus currentStatus, TransportStatus nextStatus) {
        if (currentStatus == null || nextStatus == null) {
            throw new BusinessException("运输状态不能为空");
        }
        if (currentStatus == TransportStatus.PENDING
                && (nextStatus == TransportStatus.IN_TRANSIT || nextStatus == TransportStatus.CANCELLED)) {
            return;
        }
        if (currentStatus == TransportStatus.IN_TRANSIT && nextStatus == TransportStatus.ARRIVED) {
            return;
        }
        throw new BusinessException("运输状态不允许这样流转");
    }
}
