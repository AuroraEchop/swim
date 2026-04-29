package com.gavin.shipping.auth;

import com.gavin.shipping.auth.dto.LoginRequest;
import com.gavin.shipping.auth.dto.LoginResponse;
import com.gavin.shipping.auth.dto.LoginUser;
import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.domain.UserAccount;
import com.gavin.shipping.mapper.UserMapper;
import com.gavin.shipping.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
