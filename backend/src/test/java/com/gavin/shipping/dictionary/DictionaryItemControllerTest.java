package com.gavin.shipping.dictionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.dictionary.dto.CreateDictionaryItemRequest;
import com.gavin.shipping.dictionary.dto.DictionaryItemCreateResponse;
import com.gavin.shipping.dictionary.dto.DictionaryItemResponse;
import com.gavin.shipping.dictionary.dto.UpdateDictionaryItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionaryItemController.class)
class DictionaryItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryItemService dictionaryItemService;

    @Test
    void shouldReturnDictionaryItemsByType() throws Exception {
        when(dictionaryItemService.findByType("PORT")).thenReturn(List.of(dictionaryItemResponse()));

        mockMvc.perform(get("/dictionaries/{type}", "PORT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].dictType").value("PORT"))
                .andExpect(jsonPath("$.data[0].label").value("上海港"))
                .andExpect(jsonPath("$.data[0].enabled").value(true));
    }

    @Test
    void shouldCreateDictionaryItem() throws Exception {
        when(dictionaryItemService.create(any(CreateDictionaryItemRequest.class)))
                .thenReturn(new DictionaryItemCreateResponse(1L));

        mockMvc.perform(post("/dictionaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldRejectDuplicatedDictionaryItem() throws Exception {
        when(dictionaryItemService.create(any(CreateDictionaryItemRequest.class)))
                .thenThrow(new ConflictException("字典项已存在"));

        mockMvc.perform(post("/dictionaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("字典项已存在"));
    }

    @Test
    void shouldUpdateDictionaryItem() throws Exception {
        mockMvc.perform(put("/dictionaries/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateDictionaryItemRequest(
                                "宁波舟山港",
                                "NINGBO_ZHOUSHAN_PORT",
                                3,
                                true,
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("修改成功"));
    }

    @Test
    void shouldReturnNoContentWhenDeleteDictionaryItem() throws Exception {
        mockMvc.perform(delete("/dictionaries/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDeleteWhenDictionaryItemNotFound() throws Exception {
        doThrow(new com.gavin.shipping.common.NotFoundException("字典项不存在"))
                .when(dictionaryItemService).delete(eq(99L));

        mockMvc.perform(delete("/dictionaries/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("字典项不存在"));
    }

    private CreateDictionaryItemRequest createRequest() {
        return new CreateDictionaryItemRequest(
                "PORT",
                "上海港",
                "SHANGHAI_PORT",
                1,
                true,
                "常用港口"
        );
    }

    private DictionaryItemResponse dictionaryItemResponse() {
        return new DictionaryItemResponse(
                1L,
                "PORT",
                "上海港",
                "SHANGHAI_PORT",
                1,
                true,
                "常用港口",
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 10, 0)
        );
    }
}
