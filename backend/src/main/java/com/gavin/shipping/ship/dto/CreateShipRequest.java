package com.gavin.shipping.ship.dto;

import com.gavin.shipping.domain.ShipStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateShipRequest(
        @NotBlank(message = "船舶编号不能为空")
        @Size(min = 3, max = 30, message = "船舶编号长度必须为3-30")
        String shipNo,

        @NotBlank(message = "船名不能为空")
        @Size(min = 2, max = 50, message = "船名长度必须为2-50")
        String shipName,

        @NotBlank(message = "船舶类型不能为空")
        String shipType,

        @NotNull(message = "载重量不能为空")
        @DecimalMin(value = "0.01", message = "载重量必须大于0")
        BigDecimal loadCapacity,

        @Size(max = 50, message = "所属港口长度不能超过50")
        String homePort,

        @NotNull(message = "船舶状态不能为空")
        ShipStatus status,

        @Size(max = 255, message = "备注长度不能超过255")
        String remark
) {
}
