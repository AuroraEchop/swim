package com.gavin.shipping.crew.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gavin.shipping.domain.CrewStatus;

import java.time.LocalDateTime;

public record CrewMemberResponse(
        Long id,
        String crewNo,
        String name,
        String gender,
        String phone,
        String certificateNo,
        String position,
        Long shipId,
        String shipName,
        CrewStatus status,
        String remark,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}
