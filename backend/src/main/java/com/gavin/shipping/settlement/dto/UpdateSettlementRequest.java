package com.gavin.shipping.settlement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateSettlementRequest(
        @NotNull(message = "运费金额不能为空")
        @DecimalMin(value = "0.00", message = "运费金额不能小于0")
        BigDecimal freightAmount,

        @DecimalMin(value = "0.00", message = "附加费用不能小于0")
        BigDecimal additionalFee,

        @DecimalMin(value = "0.00", message = "优惠金额不能小于0")
        BigDecimal discountAmount,

        @DecimalMin(value = "0.00", message = "实收金额不能小于0")
        BigDecimal receivedAmount,

        @Size(max = 255, message = "备注长度不能超过255")
        String remark
) {
}
