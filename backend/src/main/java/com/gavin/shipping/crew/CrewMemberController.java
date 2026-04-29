package com.gavin.shipping.crew;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.CrewStatus;
import com.gavin.shipping.crew.dto.CreateCrewMemberRequest;
import com.gavin.shipping.crew.dto.CrewMemberCreateResponse;
import com.gavin.shipping.crew.dto.CrewMemberResponse;
import com.gavin.shipping.crew.dto.UpdateCrewMemberRequest;
import com.gavin.shipping.crew.dto.UpdateCrewMemberStatusRequest;
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
@RequestMapping("/crew-members")
public class CrewMemberController {

    private final CrewMemberService crewMemberService;

    public CrewMemberController(CrewMemberService crewMemberService) {
        this.crewMemberService = crewMemberService;
    }

    @GetMapping
    public ApiResponse<PageResult<CrewMemberResponse>> findPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Long shipId,
            @RequestParam(required = false) CrewStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ApiResponse.success(crewMemberService.findPage(keyword, position, shipId, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<CrewMemberResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(crewMemberService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CrewMemberCreateResponse>> create(@Valid @RequestBody CreateCrewMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(crewMemberService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateCrewMemberRequest request) {
        crewMemberService.update(id, request);
        return ApiResponse.successMessage("修改成功");
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCrewMemberStatusRequest request
    ) {
        crewMemberService.updateStatus(id, request.status());
        return ApiResponse.successMessage("状态修改成功");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        crewMemberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
