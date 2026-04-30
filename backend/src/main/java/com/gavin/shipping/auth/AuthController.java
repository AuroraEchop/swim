package com.gavin.shipping.auth;

import com.gavin.shipping.auth.dto.CurrentUserResponse;
import com.gavin.shipping.auth.dto.LoginRequest;
import com.gavin.shipping.auth.dto.LoginResponse;
import com.gavin.shipping.auth.dto.LoginUser;
import com.gavin.shipping.auth.dto.UpdatePasswordRequest;
import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.domain.UserAccount;
import com.gavin.shipping.mapper.UserMapper;
import com.gavin.shipping.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService = new AuthService();
    private final UserMapper userMapper;

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = authService.login(request.username(), request.password(), userMapper::findByUsername);
        LoginUser loginUser = new LoginUser(user.id(), user.username(), user.realName(), user.roleCode());
        String loginToken = "demo-token-" + user.username() + "-" + user.id();
        return ApiResponse.success(new LoginResponse(loginToken, "Bearer", loginUser));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        UserAccount user = authService.currentUser(authorization, userMapper::findByUsername);
        return ApiResponse.success(new CurrentUserResponse(
                user.id(),
                user.username(),
                user.realName(),
                user.roleCode(),
                permissionsOf(user.roleCode())
        ));
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        authService.changePassword(authorization, request.oldPassword(), request.newPassword(), new AuthService.PasswordRepository() {
            @Override
            public java.util.Optional<UserAccount> findByUsername(String username) {
                return userMapper.findByUsername(username);
            }

            @Override
            public void updatePassword(Long id, String newPassword) {
                userMapper.updatePassword(id, newPassword);
            }
        });
        return ApiResponse.successMessage("密码修改成功");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.successMessage("退出成功");
    }

    private List<String> permissionsOf(String roleCode) {
        if ("ADMIN".equals(roleCode)) {
            return List.of(
                    "user:read", "user:create", "user:update", "user:delete",
                    "ship:read", "ship:create", "ship:update", "ship:delete",
                    "crew:read", "crew:create", "crew:update", "crew:delete",
                    "transport:read", "transport:create", "transport:update", "transport:delete",
                    "settlement:read", "settlement:create", "settlement:update", "settlement:delete",
                    "dictionary:manage"
            );
        }
        if ("VIEWER".equals(roleCode)) {
            return List.of("ship:read", "crew:read", "transport:read", "settlement:read");
        }
        return List.of("ship:read", "crew:read", "transport:read", "settlement:read");
    }
}
