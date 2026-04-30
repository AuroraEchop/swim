package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
import com.gavin.shipping.common.UnauthorizedException;
import com.gavin.shipping.domain.UserAccount;
import com.gavin.shipping.domain.UserStatus;

import java.util.Optional;

public class AuthService {

    public UserAccount login(String username, String password, UserRepository userRepository) {
        if (isBlank(username) || isBlank(password)) {
            throw new BusinessException("用户名或密码不能为空");
        }

        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (user.status() == UserStatus.DISABLED) {
            throw new BusinessException("用户已被禁用");
        }

        if (!password.equals(user.password())) {
            throw new BusinessException("用户名或密码错误");
        }

        return user;
    }

    public UserAccount currentUser(String authorizationHeader, UserRepository userRepository) {
        String username = resolveUsernameFromToken(authorizationHeader);
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("未登录或登录已过期"));
        if (user.status() == UserStatus.DISABLED) {
            throw new BusinessException("用户已被禁用");
        }
        return user;
    }

    public void changePassword(
            String authorizationHeader,
            String oldPassword,
            String newPassword,
            PasswordRepository passwordRepository
    ) {
        if (isBlank(oldPassword) || isBlank(newPassword)) {
            throw new BusinessException("旧密码或新密码不能为空");
        }
        UserAccount user = currentUser(authorizationHeader, passwordRepository::findByUsername);
        if (!oldPassword.equals(user.password())) {
            throw new BusinessException("旧密码错误");
        }
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        passwordRepository.updatePassword(user.id(), newPassword);
    }

    private String resolveUsernameFromToken(String authorizationHeader) {
        if (isBlank(authorizationHeader)) {
            throw new UnauthorizedException("未登录或登录已过期");
        }

        String token = authorizationHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length()).trim();
        }
        String prefix = "demo-token-";
        if (!token.startsWith(prefix)) {
            throw new UnauthorizedException("未登录或登录已过期");
        }

        String body = token.substring(prefix.length());
        int lastDashIndex = body.lastIndexOf('-');
        if (lastDashIndex <= 0 || lastDashIndex == body.length() - 1) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        String username = body.substring(0, lastDashIndex);
        String idPart = body.substring(lastDashIndex + 1);
        try {
            Long.parseLong(idPart);
        } catch (NumberFormatException exception) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        return username;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public interface UserRepository {
        Optional<UserAccount> findByUsername(String username);
    }

    public interface PasswordRepository {
        Optional<UserAccount> findByUsername(String username);

        void updatePassword(Long id, String newPassword);
    }
}
