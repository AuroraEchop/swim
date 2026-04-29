package com.gavin.shipping.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.UserStatus;
import com.gavin.shipping.user.dto.CreateUserRequest;
import com.gavin.shipping.user.dto.UpdateUserRequest;
import com.gavin.shipping.user.dto.UserCreateResponse;
import com.gavin.shipping.user.dto.UserResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserManagementController.class)
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserManagementService userManagementService;

    @Test
    void shouldReturnPagedUsers() throws Exception {
        when(userManagementService.findPage("admin", null, 1L, UserStatus.ENABLED, 1, 10))
                .thenReturn(PageResult.of(List.of(userResponse()), 1, 10, 1));

        mockMvc.perform(get("/users")
                        .param("username", "admin")
                        .param("roleId", "1")
                        .param("status", "ENABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("admin"))
                .andExpect(jsonPath("$.data.records[0].roleCode").value("ADMIN"));
    }

    @Test
    void shouldCreateUser() throws Exception {
        when(userManagementService.create(any(CreateUserRequest.class))).thenReturn(new UserCreateResponse(3L));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void shouldRejectDuplicatedUsername() throws Exception {
        when(userManagementService.create(any(CreateUserRequest.class))).thenThrow(new ConflictException("用户名已存在"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        mockMvc.perform(put("/users/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest(
                                "业务员二号",
                                "13800000009",
                                "business02@example.com",
                                2L,
                                UserStatus.DISABLED
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("修改成功"));
    }

    @Test
    void shouldRejectDeleteBuiltInAdmin() throws Exception {
        doThrow(new ConflictException("系统内置管理员账号不允许删除"))
                .when(userManagementService).delete(eq(1L));

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("系统内置管理员账号不允许删除"));
    }

    private CreateUserRequest createRequest() {
        return new CreateUserRequest(
                "business02",
                "123456",
                "业务员二号",
                "13800000009",
                "business02@example.com",
                2L,
                UserStatus.ENABLED
        );
    }

    private UserResponse userResponse() {
        return new UserResponse(
                1L,
                "admin",
                "系统管理员",
                "13800000000",
                "admin@example.com",
                1L,
                "管理员",
                "ADMIN",
                UserStatus.ENABLED,
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 10, 0)
        );
    }
}
