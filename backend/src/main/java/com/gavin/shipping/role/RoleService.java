package com.gavin.shipping.role;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.role.dto.CreateRoleRequest;
import com.gavin.shipping.role.dto.RoleCreateResponse;
import com.gavin.shipping.role.dto.RoleResponse;
import com.gavin.shipping.role.dto.UpdateRoleRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleMapper roleMapper;

    public RoleService(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public List<RoleResponse> findAll() {
        return roleMapper.findAll().stream().map(this::toResponse).toList();
    }

    public RoleCreateResponse create(CreateRoleRequest request) {
        if (roleMapper.countByRoleCode(request.roleCode()) > 0) {
            throw new ConflictException("角色编码已存在");
        }
        RoleEntity role = new RoleEntity();
        role.setRoleName(request.roleName());
        role.setRoleCode(request.roleCode());
        role.setDescription(request.description());
        role.setBuiltIn(false);
        roleMapper.insert(role);
        return new RoleCreateResponse(role.getId());
    }

    public void update(Long id, UpdateRoleRequest request) {
        RoleEntity role = roleMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("角色不存在"));
        role.setRoleName(request.roleName());
        role.setDescription(request.description());
        roleMapper.update(role);
    }

    public void delete(Long id) {
        RoleEntity role = roleMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("角色不存在"));
        if (Boolean.TRUE.equals(role.getBuiltIn())) {
            throw new ConflictException("系统内置角色不允许删除");
        }
        if (roleMapper.countUsersByRoleId(id) > 0) {
            throw new ConflictException("已被用户使用的角色不能删除");
        }
        roleMapper.deleteById(id);
    }

    private RoleResponse toResponse(RoleEntity role) {
        return new RoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getRoleCode(),
                role.getDescription(),
                role.getBuiltIn()
        );
    }
}
