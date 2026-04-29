package com.gavin.shipping.dictionary;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.dictionary.dto.CreateDictionaryItemRequest;
import com.gavin.shipping.dictionary.dto.DictionaryItemCreateResponse;
import com.gavin.shipping.dictionary.dto.DictionaryItemResponse;
import com.gavin.shipping.dictionary.dto.UpdateDictionaryItemRequest;
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
@RequestMapping("/dictionaries")
public class DictionaryItemController {

    private final DictionaryItemService dictionaryItemService;

    public DictionaryItemController(DictionaryItemService dictionaryItemService) {
        this.dictionaryItemService = dictionaryItemService;
    }

    @GetMapping("/{type}")
    public ApiResponse<List<DictionaryItemResponse>> findByType(@PathVariable String type) {
        return ApiResponse.success(dictionaryItemService.findByType(type));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DictionaryItemCreateResponse>> create(
            @Valid @RequestBody CreateDictionaryItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dictionaryItemService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateDictionaryItemRequest request) {
        dictionaryItemService.update(id, request);
        return ApiResponse.successMessage("修改成功");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dictionaryItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
