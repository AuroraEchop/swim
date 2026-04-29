package com.gavin.shipping.crew;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.CrewStatus;
import com.gavin.shipping.crew.dto.CreateCrewMemberRequest;
import com.gavin.shipping.crew.dto.CrewMemberCreateResponse;
import com.gavin.shipping.crew.dto.CrewMemberResponse;
import com.gavin.shipping.crew.dto.UpdateCrewMemberStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CrewMemberController.class)
class CrewMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrewMemberService crewMemberService;

    @Test
    void shouldReturnPagedCrewMembers() throws Exception {
        when(crewMemberService.findPage("张", "船长", 1L, CrewStatus.ON_DUTY, 1, 10))
                .thenReturn(PageResult.of(List.of(crewMemberResponse()), 1, 10, 1));

        mockMvc.perform(get("/crew-members")
                        .param("keyword", "张")
                        .param("position", "船长")
                        .param("shipId", "1")
                        .param("status", "ON_DUTY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].crewNo").value("CREW-001"))
                .andExpect(jsonPath("$.data.records[0].shipName").value("远航一号"));
    }

    @Test
    void shouldCreateCrewMember() throws Exception {
        when(crewMemberService.create(any(CreateCrewMemberRequest.class))).thenReturn(new CrewMemberCreateResponse(3L));

        mockMvc.perform(post("/crew-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void shouldRejectDuplicatedCrewNo() throws Exception {
        when(crewMemberService.create(any(CreateCrewMemberRequest.class))).thenThrow(new ConflictException("船员编号已存在"));

        mockMvc.perform(post("/crew-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("船员编号已存在"));
    }

    @Test
    void shouldUpdateCrewMemberStatus() throws Exception {
        mockMvc.perform(patch("/crew-members/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCrewMemberStatusRequest(CrewStatus.ON_LEAVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("状态修改成功"));
    }

    @Test
    void shouldReturnNoContentWhenDeleteCrewMember() throws Exception {
        mockMvc.perform(delete("/crew-members/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDeleteWhenCrewMemberIsBusy() throws Exception {
        doThrow(new ConflictException("当前在岗且所属船舶正在运输中的船员不能删除")).when(crewMemberService).delete(eq(1L));

        mockMvc.perform(delete("/crew-members/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("当前在岗且所属船舶正在运输中的船员不能删除"));
    }

    private CreateCrewMemberRequest createRequest() {
        return new CreateCrewMemberRequest(
                "CREW-003",
                "王五",
                "男",
                "13800000004",
                "CERT-003",
                "轮机长",
                1L,
                CrewStatus.ON_DUTY,
                "新增船员"
        );
    }

    private CrewMemberResponse crewMemberResponse() {
        return new CrewMemberResponse(
                1L,
                "CREW-001",
                "张三",
                "男",
                "13800000002",
                "CERT-001",
                "船长",
                1L,
                "远航一号",
                CrewStatus.ON_DUTY,
                "经验丰富",
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 10, 0)
        );
    }
}
