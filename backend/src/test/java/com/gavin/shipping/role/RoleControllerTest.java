package com.gavin.shipping.role;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.role.dto.CreateRoleRequest;
import com.gavin.shipping.role.dto.RoleCreateResponse;
import com.gavin.shipping.role.dto.RoleResponse;
import com.gavin.shipping.role.dto.UpdateRoleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @Test
    void shouldReturnRoles() throws Exception {
        when(roleService.findAll()).thenReturn(List.of(new RoleResponse(
                2L,
                "业务用户",
                "BUSINESS",
                "负责业务数据维护",
                true
        )));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roleCode").value("BUSINESS"))
                .andExpect(jsonPath("$.data[0].builtIn").value(true));
    }

    @Test
    void shouldCreateRole() throws Exception {
        when(roleService.create(any(CreateRoleRequest.class))).thenReturn(new RoleCreateResponse(3L));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRoleRequest(
                                "查看用户",
                                "VIEWER",
                                "仅允许查看业务数据",
                                List.of("ship:read")
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void shouldRejectDuplicatedRoleCode() throws Exception {
        when(roleService.create(any(CreateRoleRequest.class))).thenThrow(new ConflictException("角色编码已存在"));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRoleRequest(
                                "查看用户",
                                "VIEWER",
                                null,
                                null
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("角色编码已存在"));
    }

    @Test
    void shouldUpdateRole() throws Exception {
        mockMvc.perform(put("/roles/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRoleRequest(
                                "业务管理员",
                                "负责业务维护",
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("修改成功"));
    }

    @Test
    void shouldRejectDeleteBuiltInRole() throws Exception {
        doThrow(new ConflictException("系统内置角色不允许删除")).when(roleService).delete(eq(1L));

        mockMvc.perform(delete("/roles/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("系统内置角色不允许删除"));
    }
}
