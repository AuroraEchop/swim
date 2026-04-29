package com.gavin.shipping.ship;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.ShipStatus;
import com.gavin.shipping.ship.dto.CreateShipRequest;
import com.gavin.shipping.ship.dto.ShipCreateResponse;
import com.gavin.shipping.ship.dto.ShipResponse;
import com.gavin.shipping.ship.dto.UpdateShipRequest;
import com.gavin.shipping.ship.dto.UpdateShipStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ships")
public class ShipController {

    private final ShipService shipService;

    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping
    public ApiResponse<PageResult<ShipResponse>> findPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "type") String type,
            @RequestParam(required = false) String homePort,
            @RequestParam(required = false) ShipStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ApiResponse.success(shipService.findPage(keyword, type, homePort, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShipResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(shipService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShipCreateResponse>> create(@Valid @RequestBody CreateShipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(shipService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateShipRequest request) {
        shipService.update(id, request);
        return ApiResponse.successMessage("修改成功");
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShipStatusRequest request
    ) {
        shipService.updateStatus(id, request.status());
        return ApiResponse.successMessage("状态修改成功");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shipService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
