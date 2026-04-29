package com.gavin.shipping.role;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.role.dto.CreateRoleRequest;
import com.gavin.shipping.role.dto.RoleCreateResponse;
import com.gavin.shipping.role.dto.RoleResponse;
import com.gavin.shipping.role.dto.UpdateRoleRequest;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> findAll() {
        return ApiResponse.success(roleService.findAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleCreateResponse>> create(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(roleService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        roleService.update(id, request);
        return ApiResponse.successMessage("修改成功");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
