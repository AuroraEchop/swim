package com.gavin.shipping.dictionary;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.dictionary.dto.CreateDictionaryItemRequest;
import com.gavin.shipping.dictionary.dto.DictionaryItemCreateResponse;
import com.gavin.shipping.dictionary.dto.DictionaryItemResponse;
import com.gavin.shipping.dictionary.dto.UpdateDictionaryItemRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryItemService {

    private final DictionaryItemMapper dictionaryItemMapper;

    public DictionaryItemService(DictionaryItemMapper dictionaryItemMapper) {
        this.dictionaryItemMapper = dictionaryItemMapper;
    }

    public List<DictionaryItemResponse> findByType(String type) {
        return dictionaryItemMapper.findByType(type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DictionaryItemCreateResponse create(CreateDictionaryItemRequest request) {
        if (dictionaryItemMapper.countByTypeAndValue(request.dictType(), request.value()) > 0) {
            throw new ConflictException("字典项已存在");
        }

        DictionaryItemEntity dictionaryItem = new DictionaryItemEntity();
        dictionaryItem.setDictType(request.dictType());
        dictionaryItem.setLabel(request.label());
        dictionaryItem.setValue(request.value());
        dictionaryItem.setSort(defaultSort(request.sort()));
        dictionaryItem.setEnabled(defaultEnabled(request.enabled()));
        dictionaryItem.setRemark(request.remark());
        dictionaryItemMapper.insert(dictionaryItem);
        return new DictionaryItemCreateResponse(dictionaryItem.getId());
    }

    public void update(Long id, UpdateDictionaryItemRequest request) {
        DictionaryItemEntity dictionaryItem = dictionaryItemMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("字典项不存在"));
        if (dictionaryItemMapper.countByTypeAndValueExcludingId(dictionaryItem.getDictType(), request.value(), id) > 0) {
            throw new ConflictException("字典项已存在");
        }

        dictionaryItem.setLabel(request.label());
        dictionaryItem.setValue(request.value());
        dictionaryItem.setSort(defaultSort(request.sort()));
        dictionaryItem.setEnabled(defaultEnabled(request.enabled()));
        dictionaryItem.setRemark(request.remark());
        dictionaryItemMapper.update(dictionaryItem);
    }

    public void delete(Long id) {
        if (dictionaryItemMapper.findById(id).isEmpty()) {
            throw new NotFoundException("字典项不存在");
        }
        dictionaryItemMapper.deleteById(id);
    }

    private int defaultSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    private boolean defaultEnabled(Boolean enabled) {
        return enabled == null || enabled;
    }

    private DictionaryItemResponse toResponse(DictionaryItemEntity dictionaryItem) {
        return new DictionaryItemResponse(
                dictionaryItem.getId(),
                dictionaryItem.getDictType(),
                dictionaryItem.getLabel(),
                dictionaryItem.getValue(),
                dictionaryItem.getSort(),
                dictionaryItem.getEnabled(),
                dictionaryItem.getRemark(),
                dictionaryItem.getCreatedAt(),
                dictionaryItem.getUpdatedAt()
        );
    }
}
