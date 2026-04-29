package com.gavin.shipping.user;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.UserStatus;
import com.gavin.shipping.user.dto.CreateUserRequest;
import com.gavin.shipping.user.dto.UpdateUserRequest;
import com.gavin.shipping.user.dto.UserCreateResponse;
import com.gavin.shipping.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ApiResponse<PageResult<UserResponse>> findPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ApiResponse.success(userManagementService.findPage(username, realName, roleId, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(userManagementService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserCreateResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(userManagementService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        userManagementService.update(id, request);
        return ApiResponse.successMessage("修改成功");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userManagementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
