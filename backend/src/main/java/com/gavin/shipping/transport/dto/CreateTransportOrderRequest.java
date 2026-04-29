package com.gavin.shipping.transport.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTransportOrderRequest(
        @NotBlank(message = "货物名称不能为空")
        @Size(min = 2, max = 100, message = "货物名称长度必须为2-100")
        String cargoName,

        @NotBlank(message = "货物类型不能为空")
        String cargoType,

        @NotNull(message = "货物重量不能为空")
        @DecimalMin(value = "0.01", message = "货物重量必须大于0")
        BigDecimal cargoWeight,

        @NotBlank(message = "起运港不能为空")
        @Size(max = 50, message = "起运港长度不能超过50")
        String originPort,

        @NotBlank(message = "目的港不能为空")
        @Size(max = 50, message = "目的港长度不能超过50")
        String destinationPort,

        @NotNull(message = "运输船舶不能为空")
        Long shipId,

        @NotBlank(message = "客户名称不能为空")
        @Size(min = 2, max = 100, message = "客户名称长度必须为2-100")
        String customerName,

        @Size(max = 30, message = "客户电话长度不能超过30")
        String customerPhone,

        @NotNull(message = "预计出发时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime plannedDepartureTime,

        @NotNull(message = "预计到达时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime plannedArrivalTime,

        @Size(max = 255, message = "备注长度不能超过255")
        String remark
) {
}
