package com.gavin.shipping.ship.dto;

import com.gavin.shipping.domain.ShipStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipStatusRequest(
        @NotNull(message = "船舶状态不能为空")
        ShipStatus status
) {
}
