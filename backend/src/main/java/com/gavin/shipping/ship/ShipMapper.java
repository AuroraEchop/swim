package com.gavin.shipping.ship;

import com.gavin.shipping.domain.ShipStatus;
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
public interface ShipMapper {

    @Select("""
            <script>
            SELECT id, ship_no, ship_name, ship_type, load_capacity, home_port, status, remark, created_at, updated_at
            FROM ship
            <where>
              <if test="keyword != null and keyword != ''">
                AND (ship_no LIKE CONCAT('%', #{keyword}, '%') OR ship_name LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="type != null and type != ''">
                AND ship_type = #{type}
              </if>
              <if test="homePort != null and homePort != ''">
                AND home_port = #{homePort}
              </if>
              <if test="status != null">
                AND status = #{status}
              </if>
            </where>
            ORDER BY id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<ShipEntity> findPage(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("homePort") String homePort,
            @Param("status") ShipStatus status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM ship
            <where>
              <if test="keyword != null and keyword != ''">
                AND (ship_no LIKE CONCAT('%', #{keyword}, '%') OR ship_name LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="type != null and type != ''">
                AND ship_type = #{type}
              </if>
              <if test="homePort != null and homePort != ''">
                AND home_port = #{homePort}
              </if>
              <if test="status != null">
                AND status = #{status}
              </if>
            </where>
            </script>
            """)
    long count(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("homePort") String homePort,
            @Param("status") ShipStatus status
    );

    @Select("""
            SELECT id, ship_no, ship_name, ship_type, load_capacity, home_port, status, remark, created_at, updated_at
            FROM ship
            WHERE id = #{id}
            """)
    Optional<ShipEntity> findById(Long id);

    @Select("SELECT COUNT(*) FROM ship WHERE ship_no = #{shipNo}")
    int countByShipNo(String shipNo);

    @Insert("""
            INSERT INTO ship (ship_no, ship_name, ship_type, load_capacity, home_port, status, remark)
            VALUES (#{shipNo}, #{shipName}, #{shipType}, #{loadCapacity}, #{homePort}, #{status}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShipEntity ship);

    @Update("""
            UPDATE ship
            SET ship_name = #{shipName},
                ship_type = #{shipType},
                load_capacity = #{loadCapacity},
                home_port = #{homePort},
                status = #{status},
                remark = #{remark}
            WHERE id = #{id}
            """)
    int update(ShipEntity ship);

    @Update("UPDATE ship SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") ShipStatus status);

    @Select("""
            SELECT COUNT(*)
            FROM transport_order
            WHERE ship_id = #{shipId}
              AND status IN ('PENDING', 'IN_TRANSIT')
            """)
    int countActiveTransportOrders(Long shipId);

    @Delete("DELETE FROM ship WHERE id = #{id}")
    int deleteById(Long id);
}
