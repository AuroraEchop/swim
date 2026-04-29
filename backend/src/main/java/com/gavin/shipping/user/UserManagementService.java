package com.gavin.shipping.user;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.UserStatus;
import com.gavin.shipping.user.dto.CreateUserRequest;
import com.gavin.shipping.user.dto.UpdateUserRequest;
import com.gavin.shipping.user.dto.UserCreateResponse;
import com.gavin.shipping.user.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserManagementService {

    private final UserManagementMapper userManagementMapper;

    public UserManagementService(UserManagementMapper userManagementMapper) {
        this.userManagementMapper = userManagementMapper;
    }

    public PageResult<UserResponse> findPage(
            String username,
            String realName,
            Long roleId,
            UserStatus status,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (safePage - 1) * safePageSize;
        long total = userManagementMapper.count(username, realName, roleId, status);
        List<UserResponse> records = userManagementMapper.findPage(username, realName, roleId, status, offset, safePageSize)
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResult.of(records, safePage, safePageSize, total);
    }

    public UserResponse findById(Long id) {
        return userManagementMapper.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
    }

    public UserCreateResponse create(CreateUserRequest request) {
        if (userManagementMapper.countByUsername(request.username()) > 0) {
            throw new ConflictException("用户名已存在");
        }
        ensureRoleExists(request.roleId());

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setRoleId(request.roleId());
        user.setStatus(request.status());
        userManagementMapper.insert(user);
        return new UserCreateResponse(user.getId());
    }

    public void update(Long id, UpdateUserRequest request) {
        UserEntity user = userManagementMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        ensureRoleExists(request.roleId());

        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setRoleId(request.roleId());
        user.setStatus(request.status());
        userManagementMapper.update(user);
    }

    public void delete(Long id) {
        UserEntity user = userManagementMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        if ("admin".equals(user.getUsername()) || Long.valueOf(1L).equals(user.getId())) {
            throw new ConflictException("系统内置管理员账号不允许删除");
        }
        userManagementMapper.deleteById(id);
    }

    private void ensureRoleExists(Long roleId) {
        if (userManagementMapper.countRoleById(roleId) == 0) {
            throw new NotFoundException("角色不存在");
        }
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getPhone(),
                user.getEmail(),
                user.getRoleId(),
                user.getRoleName(),
                user.getRoleCode(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
