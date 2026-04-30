package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.common.UnauthorizedException;
import com.gavin.shipping.domain.UserAccount;
import com.gavin.shipping.domain.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

    @Test
    void shouldResolveCurrentUserFromDemoToken() {
        UserAccount user = authService.currentUser("Bearer demo-token-admin-1", repository());

        assertThat(user.username()).isEqualTo("admin");
    }

    @Test
    void shouldRejectMissingToken() {
        assertThatThrownBy(() -> authService.currentUser(null, repository()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("未登录或登录已过期");
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThatThrownBy(() -> authService.currentUser("bad-token", repository()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("未登录或登录已过期");
    }

    @Test
    void shouldChangePasswordWithPlainTextValue() {
        AtomicReference<String> updatedPassword = new AtomicReference<>();

        authService.changePassword(
                "Bearer demo-token-admin-1",
                "123456",
                "654321",
                new AuthService.PasswordRepository() {
                    @Override
                    public Optional<UserAccount> findByUsername(String username) {
                        return repository().findByUsername(username);
                    }

                    @Override
                    public void updatePassword(Long id, String newPassword) {
                        updatedPassword.set(newPassword);
                    }
                }
        );

        assertThat(updatedPassword.get()).isEqualTo("654321");
    }

    @Test
    void shouldRejectSameNewPassword() {
        assertThatThrownBy(() -> authService.changePassword(
                "Bearer demo-token-admin-1",
                "123456",
                "123456",
                passwordRepository()
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("新密码不能与旧密码相同");
    }

    private AuthService.UserRepository repository() {
        Map<String, UserAccount> users = Map.of(
                "admin", new UserAccount(1L, "admin", "123456", "系统管理员", "ADMIN", UserStatus.ENABLED),
                "disabled", new UserAccount(2L, "disabled", "123456", "停用用户", "VIEWER", UserStatus.DISABLED)
        );
        return username -> Optional.ofNullable(users.get(username));
    }

    private AuthService.PasswordRepository passwordRepository() {
        Map<String, UserAccount> users = new HashMap<>();
        users.put("admin", new UserAccount(1L, "admin", "123456", "系统管理员", "ADMIN", UserStatus.ENABLED));
        return new AuthService.PasswordRepository() {
            @Override
            public Optional<UserAccount> findByUsername(String username) {
                return Optional.ofNullable(users.get(username));
            }

            @Override
            public void updatePassword(Long id, String newPassword) {
                users.computeIfPresent("admin", (username, user) -> new UserAccount(
                        user.id(),
                        user.username(),
                        newPassword,
                        user.realName(),
                        user.roleCode(),
                        user.status()
                ));
            }
        };
    }
}
