package com.gavin.shipping.dictionary;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DictionaryItemMapper {

    @Select("""
            SELECT id, dict_type, label, value, sort, enabled, remark, created_at, updated_at
            FROM dictionary_item
            WHERE dict_type = #{type}
            ORDER BY sort ASC, id ASC
            """)
    List<DictionaryItemEntity> findByType(String type);

    @Select("""
            SELECT id, dict_type, label, value, sort, enabled, remark, created_at, updated_at
            FROM dictionary_item
            WHERE id = #{id}
            """)
    Optional<DictionaryItemEntity> findById(Long id);

    @Select("SELECT COUNT(*) FROM dictionary_item WHERE dict_type = #{dictType} AND value = #{value}")
    int countByTypeAndValue(@Param("dictType") String dictType, @Param("value") String value);

    @Select("""
            SELECT COUNT(*)
            FROM dictionary_item
            WHERE dict_type = #{dictType}
              AND value = #{value}
              AND id != #{id}
            """)
    int countByTypeAndValueExcludingId(
            @Param("dictType") String dictType,
            @Param("value") String value,
            @Param("id") Long id
    );

    @Insert("""
            INSERT INTO dictionary_item (dict_type, label, value, sort, enabled, remark)
            VALUES (#{dictType}, #{label}, #{value}, #{sort}, #{enabled}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DictionaryItemEntity dictionaryItem);

    @Update("""
            UPDATE dictionary_item
            SET label = #{label},
                value = #{value},
                sort = #{sort},
                enabled = #{enabled},
                remark = #{remark}
            WHERE id = #{id}
            """)
    int update(DictionaryItemEntity dictionaryItem);

    @Delete("DELETE FROM dictionary_item WHERE id = #{id}")
    int deleteById(Long id);
}
