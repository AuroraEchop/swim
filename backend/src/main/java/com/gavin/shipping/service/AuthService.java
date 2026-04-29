package com.gavin.shipping.service;

import com.gavin.shipping.common.BusinessException;
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public interface UserRepository {
        Optional<UserAccount> findByUsername(String username);
    }
}
