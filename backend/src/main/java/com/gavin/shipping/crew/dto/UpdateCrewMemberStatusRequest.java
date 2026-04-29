package com.gavin.shipping.crew.dto;

import com.gavin.shipping.domain.CrewStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCrewMemberStatusRequest(
        @NotNull(message = "船员状态不能为空")
        CrewStatus status
) {
}
