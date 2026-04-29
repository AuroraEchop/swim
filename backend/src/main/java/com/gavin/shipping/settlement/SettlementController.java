package com.gavin.shipping.settlement;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.SettlementStatus;
import com.gavin.shipping.settlement.dto.CreateSettlementRequest;
import com.gavin.shipping.settlement.dto.SettlementCreateResponse;
import com.gavin.shipping.settlement.dto.SettlementPaymentResponse;
import com.gavin.shipping.settlement.dto.SettlementResponse;
import com.gavin.shipping.settlement.dto.UpdateSettlementPaymentRequest;
import com.gavin.shipping.settlement.dto.UpdateSettlementRequest;
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
@RequestMapping("/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public ApiResponse<PageResult<SettlementResponse>> findPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long transportOrderId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ApiResponse.success(settlementService.findPage(
                keyword, transportOrderId, customerName, status, startDate, endDate, page, pageSize
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<SettlementResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(settlementService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SettlementCreateResponse>> create(
            @Valid @RequestBody CreateSettlementRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(settlementService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateSettlementRequest request) {
        settlementService.update(id, request);
        return ApiResponse.successMessage("修改成功");
    }

    @PatchMapping("/{id}/payment")
    public ApiResponse<SettlementPaymentResponse> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSettlementPaymentRequest request
    ) {
        return ApiResponse.success(settlementService.updatePayment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        settlementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
