package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.domain.UserAccount;
import com.gavin.shipping.domain.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    void shouldLoginWithPlainTextPassword() {
        UserAccount user = authService.login("admin", "123456", repository());

        assertThat(user.username()).isEqualTo("admin");
        assertThat(user.password()).isEqualTo("123456");
    }

    @Test
    void shouldRejectWrongPassword() {
        assertThatThrownBy(() -> authService.login("admin", "wrong", repository()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名或密码错误");
    }

    @Test
    void shouldRejectDisabledUser() {
        assertThatThrownBy(() -> authService.login("disabled", "123456", repository()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户已被禁用");
    }

    private AuthService.UserRepository repository() {
        Map<String, UserAccount> users = Map.of(
                "admin", new UserAccount(1L, "admin", "123456", "系统管理员", "ADMIN", UserStatus.ENABLED),
                "disabled", new UserAccount(2L, "disabled", "123456", "停用用户", "VIEWER", UserStatus.DISABLED)
        );
        return username -> Optional.ofNullable(users.get(username));
    }
}
