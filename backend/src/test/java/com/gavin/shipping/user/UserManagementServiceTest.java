package com.gavin.shipping.user;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.UserStatus;
import com.gavin.shipping.user.dto.CreateUserRequest;
import com.gavin.shipping.user.dto.UpdateUserRequest;
import com.gavin.shipping.user.dto.UserCreateResponse;
import com.gavin.shipping.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
class UserManagementServiceTest {

    @Mock
    private UserManagementMapper userManagementMapper;

    @InjectMocks
    private UserManagementService userManagementService;

    @Test
    void shouldReturnPagedUsers() {
        when(userManagementMapper.count("admin", null, 1L, UserStatus.ENABLED)).thenReturn(1L);
        when(userManagementMapper.findPage("admin", null, 1L, UserStatus.ENABLED, 0, 10))
                .thenReturn(List.of(userEntity()));

        PageResult<UserResponse> result = userManagementService.findPage(
                "admin", null, 1L, UserStatus.ENABLED, 1, 10
        );

        assertThat(result.total()).isEqualTo(1L);
        assertThat(result.records().get(0).username()).isEqualTo("admin");
        assertThat(result.records().get(0).roleCode()).isEqualTo("ADMIN");
    }

    @Test
    void shouldCreateUserWithPlaintextPassword() {
        when(userManagementMapper.countByUsername("business02")).thenReturn(0);
        when(userManagementMapper.countRoleById(2L)).thenReturn(1);
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(3L);
            return 1;
        }).when(userManagementMapper).insert(any(UserEntity.class));

        UserCreateResponse response = userManagementService.create(createRequest());

        assertThat(response.id()).isEqualTo(3L);
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userManagementMapper).insert(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("123456");
    }

    @Test
    void shouldRejectDuplicatedUsername() {
        when(userManagementMapper.countByUsername("business02")).thenReturn(1);

        assertThatThrownBy(() -> userManagementService.create(createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("用户名已存在");
        verify(userManagementMapper, never()).insert(any());
    }

    @Test
    void shouldRejectCreateWhenRoleNotFound() {
        when(userManagementMapper.countByUsername("business02")).thenReturn(0);
        when(userManagementMapper.countRoleById(2L)).thenReturn(0);

        assertThatThrownBy(() -> userManagementService.create(createRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("角色不存在");
    }

    @Test
    void shouldUpdateUser() {
        when(userManagementMapper.findById(2L)).thenReturn(Optional.of(userEntity()));
        when(userManagementMapper.countRoleById(2L)).thenReturn(1);

        userManagementService.update(2L, new UpdateUserRequest(
                "业务员二号",
                "13800000009",
                "business02@example.com",
                2L,
                UserStatus.DISABLED
        ));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userManagementMapper).update(captor.capture());
        assertThat(captor.getValue().getRealName()).isEqualTo("业务员二号");
        assertThat(captor.getValue().getStatus()).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    void shouldRejectDeleteBuiltInAdmin() {
        when(userManagementMapper.findById(1L)).thenReturn(Optional.of(userEntity()));

        assertThatThrownBy(() -> userManagementService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("系统内置管理员账号不允许删除");
        verify(userManagementMapper, never()).deleteById(1L);
    }

    @Test
    void shouldDeleteUser() {
        UserEntity user = userEntity();
        user.setId(2L);
        user.setUsername("business01");
        when(userManagementMapper.findById(2L)).thenReturn(Optional.of(user));

        userManagementService.delete(2L);

        verify(userManagementMapper).deleteById(2L);
    }

    private CreateUserRequest createRequest() {
        return new CreateUserRequest(
                "business02",
                "123456",
                "业务员二号",
                "13800000009",
                "business02@example.com",
                2L,
                UserStatus.ENABLED
        );
    }

    private UserEntity userEntity() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("123456");
        user.setRealName("系统管理员");
        user.setPhone("13800000000");
        user.setEmail("admin@example.com");
        user.setRoleId(1L);
        user.setRoleName("管理员");
        user.setRoleCode("ADMIN");
        user.setStatus(UserStatus.ENABLED);
        user.setCreatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        return user;
    }
}
