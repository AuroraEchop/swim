package com.gavin.shipping.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavin.shipping.domain.UserAccount;
import com.gavin.shipping.domain.UserStatus;
import com.gavin.shipping.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserMapper userMapper;

    @Test
    void shouldLoginWithPlainTextPassword() throws Exception {
        when(userMapper.findByUsername("admin"))
                .thenReturn(Optional.of(new UserAccount(1L, "admin", "123456", "系统管理员", "ADMIN", UserStatus.ENABLED)));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload("admin", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.loginToken").value("demo-token-admin-1"))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.roleCode").value("ADMIN"));
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        when(userMapper.findByUsername("admin"))
                .thenReturn(Optional.of(new UserAccount(1L, "admin", "123456", "系统管理员", "ADMIN", UserStatus.ENABLED)));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload("admin", "wrong-password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    private record LoginPayload(String username, String password) {
    }
}
