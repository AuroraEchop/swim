package com.gavin.shipping.transport;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.TransportStatus;
import com.gavin.shipping.transport.dto.CreateTransportOrderRequest;
import com.gavin.shipping.transport.dto.TransportOrderCreateResponse;
import com.gavin.shipping.transport.dto.TransportOrderResponse;
import com.gavin.shipping.transport.dto.UpdateTransportOrderRequest;
import com.gavin.shipping.transport.dto.UpdateTransportStatusRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;

@RestController
@RequestMapping("/transport-orders")
public class TransportOrderController {

    private final TransportOrderService transportOrderService;

    public TransportOrderController(TransportOrderService transportOrderService) {
        this.transportOrderService = transportOrderService;
    }

    @GetMapping
    public ApiResponse<PageResult<TransportOrderResponse>> findPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long shipId,
            @RequestParam(required = false) String originPort,
            @RequestParam(required = false) String destinationPort,
            @RequestParam(required = false) TransportStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ApiResponse.success(transportOrderService.findPage(
                keyword, shipId, originPort, destinationPort, status, startDate, endDate, page, pageSize
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransportOrderResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(transportOrderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransportOrderCreateResponse>> create(
            @Valid @RequestBody CreateTransportOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(transportOrderService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateTransportOrderRequest request) {
        transportOrderService.update(id, request);
        return ApiResponse.successMessage("修改成功");
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransportStatusRequest request
    ) {
        transportOrderService.updateStatus(id, request);
        return ApiResponse.successMessage("状态修改成功");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transportOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
