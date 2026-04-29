package com.gavin.shipping.crew.dto;

import com.gavin.shipping.domain.CrewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCrewMemberRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(min = 2, max = 30, message = "姓名长度必须为2-30")
        String name,

        @Size(max = 10, message = "性别长度不能超过10")
        String gender,

        @Size(max = 30, message = "联系电话长度不能超过30")
        String phone,

        @NotBlank(message = "证件编号不能为空")
        @Size(max = 50, message = "证件编号长度不能超过50")
        String certificateNo,

        @NotBlank(message = "岗位不能为空")
        @Size(max = 50, message = "岗位长度不能超过50")
        String position,

        Long shipId,

        @NotNull(message = "船员状态不能为空")
        CrewStatus status,

        @Size(max = 255, message = "备注长度不能超过255")
        String remark
) {
}
