package com.gavin.shipping.dictionary;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.dictionary.dto.CreateDictionaryItemRequest;
import com.gavin.shipping.dictionary.dto.DictionaryItemCreateResponse;
import com.gavin.shipping.dictionary.dto.DictionaryItemResponse;
import com.gavin.shipping.dictionary.dto.UpdateDictionaryItemRequest;
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
class DictionaryItemServiceTest {

    @Mock
    private DictionaryItemMapper dictionaryItemMapper;

    @InjectMocks
    private DictionaryItemService dictionaryItemService;

    @Test
    void shouldReturnDictionaryItemsByType() {
        when(dictionaryItemMapper.findByType("PORT")).thenReturn(List.of(dictionaryItem()));

        List<DictionaryItemResponse> result = dictionaryItemService.findByType("PORT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).label()).isEqualTo("上海港");
        assertThat(result.get(0).enabled()).isTrue();
    }

    @Test
    void shouldCreateDictionaryItem() {
        when(dictionaryItemMapper.countByTypeAndValue("PORT", "SHANGHAI_PORT")).thenReturn(0);
        doAnswer(invocation -> {
            DictionaryItemEntity item = invocation.getArgument(0);
            item.setId(1L);
            return 1;
        }).when(dictionaryItemMapper).insert(any(DictionaryItemEntity.class));

        DictionaryItemCreateResponse response = dictionaryItemService.create(createRequest());

        assertThat(response.id()).isEqualTo(1L);
        ArgumentCaptor<DictionaryItemEntity> captor = ArgumentCaptor.forClass(DictionaryItemEntity.class);
        verify(dictionaryItemMapper).insert(captor.capture());
        assertThat(captor.getValue().getSort()).isZero();
        assertThat(captor.getValue().getEnabled()).isTrue();
    }

    @Test
    void shouldRejectDuplicatedDictionaryItem() {
        when(dictionaryItemMapper.countByTypeAndValue("PORT", "SHANGHAI_PORT")).thenReturn(1);

        assertThatThrownBy(() -> dictionaryItemService.create(createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("字典项已存在");
        verify(dictionaryItemMapper, never()).insert(any());
    }

    @Test
    void shouldUpdateDictionaryItem() {
        when(dictionaryItemMapper.findById(1L)).thenReturn(Optional.of(dictionaryItem()));
        when(dictionaryItemMapper.countByTypeAndValueExcludingId("PORT", "NINGBO_ZHOUSHAN_PORT", 1L)).thenReturn(0);

        dictionaryItemService.update(1L, new UpdateDictionaryItemRequest(
                "宁波舟山港",
                "NINGBO_ZHOUSHAN_PORT",
                3,
                false,
                "备用港口"
        ));

        ArgumentCaptor<DictionaryItemEntity> captor = ArgumentCaptor.forClass(DictionaryItemEntity.class);
        verify(dictionaryItemMapper).update(captor.capture());
        assertThat(captor.getValue().getDictType()).isEqualTo("PORT");
        assertThat(captor.getValue().getLabel()).isEqualTo("宁波舟山港");
        assertThat(captor.getValue().getEnabled()).isFalse();
    }

    @Test
    void shouldRejectUpdateWhenDictionaryItemNotFound() {
        when(dictionaryItemMapper.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dictionaryItemService.update(99L, new UpdateDictionaryItemRequest(
                "宁波舟山港",
                "NINGBO_ZHOUSHAN_PORT",
                3,
                true,
                null
        )))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("字典项不存在");
    }

    @Test
    void shouldDeleteDictionaryItem() {
        when(dictionaryItemMapper.findById(1L)).thenReturn(Optional.of(dictionaryItem()));

        dictionaryItemService.delete(1L);

        verify(dictionaryItemMapper).deleteById(1L);
    }

    private CreateDictionaryItemRequest createRequest() {
        return new CreateDictionaryItemRequest(
                "PORT",
                "上海港",
                "SHANGHAI_PORT",
                null,
                null,
                "常用港口"
        );
    }

    private DictionaryItemEntity dictionaryItem() {
        DictionaryItemEntity item = new DictionaryItemEntity();
        item.setId(1L);
        item.setDictType("PORT");
        item.setLabel("上海港");
        item.setValue("SHANGHAI_PORT");
        item.setSort(1);
        item.setEnabled(true);
        item.setRemark("常用港口");
        item.setCreatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        item.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        return item;
    }
}
