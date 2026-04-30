package com.gavin.shipping.role;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.role.dto.CreateRoleRequest;
import com.gavin.shipping.role.dto.RoleCreateResponse;
import com.gavin.shipping.role.dto.RoleResponse;
import com.gavin.shipping.role.dto.UpdateRoleRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    @Test
    void shouldReturnRoles() {
        when(roleMapper.findAll()).thenReturn(List.of(roleEntity()));

        List<RoleResponse> result = roleService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).roleCode()).isEqualTo("BUSINESS");
    }

    @Test
    void shouldCreateRole() {
        when(roleMapper.countByRoleCode("VIEWER")).thenReturn(0);
        doAnswer(invocation -> {
            RoleEntity role = invocation.getArgument(0);
            role.setId(3L);
            return 1;
        }).when(roleMapper).insert(any(RoleEntity.class));

        RoleCreateResponse response = roleService.create(new CreateRoleRequest(
                "查看用户",
                "VIEWER",
                "仅允许查看业务数据",
                List.of("ship:read")
        ));

        assertThat(response.id()).isEqualTo(3L);
        ArgumentCaptor<RoleEntity> captor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleMapper).insert(captor.capture());
        assertThat(captor.getValue().getBuiltIn()).isFalse();
    }

    @Test
    void shouldRejectDuplicatedRoleCode() {
        when(roleMapper.countByRoleCode("VIEWER")).thenReturn(1);

        assertThatThrownBy(() -> roleService.create(new CreateRoleRequest(
                "查看用户",
                "VIEWER",
                null,
                null
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessage("角色编码已存在");
        verify(roleMapper, never()).insert(any());
    }

    @Test
    void shouldUpdateRole() {
        when(roleMapper.findById(2L)).thenReturn(Optional.of(roleEntity()));

        roleService.update(2L, new UpdateRoleRequest("业务管理员", "负责业务维护", null));

        ArgumentCaptor<RoleEntity> captor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleMapper).update(captor.capture());
        assertThat(captor.getValue().getRoleName()).isEqualTo("业务管理员");
    }

    @Test
    void shouldRejectDeleteBuiltInRole() {
        RoleEntity role = roleEntity();
        role.setBuiltIn(true);
        when(roleMapper.findById(2L)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> roleService.delete(2L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("系统内置角色不允许删除");
        verify(roleMapper, never()).deleteById(2L);
    }

    @Test
    void shouldRejectDeleteRoleUsedByUser() {
        RoleEntity role = roleEntity();
        role.setBuiltIn(false);
        when(roleMapper.findById(2L)).thenReturn(Optional.of(role));
        when(roleMapper.countUsersByRoleId(2L)).thenReturn(1);

        assertThatThrownBy(() -> roleService.delete(2L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("已被用户使用的角色不能删除");
    }

    @Test
    void shouldRejectUpdateWhenRoleNotFound() {
        when(roleMapper.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.update(99L, new UpdateRoleRequest("业务管理员", null, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("角色不存在");
    }

    private RoleEntity roleEntity() {
        RoleEntity role = new RoleEntity();
        role.setId(2L);
        role.setRoleName("业务用户");
        role.setRoleCode("BUSINESS");
        role.setDescription("可查看业务数据");
        role.setBuiltIn(false);
        return role;
    }
}
