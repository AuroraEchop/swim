package com.gavin.shipping.transport;

import com.gavin.shipping.domain.TransportStatus;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TransportOrderMapper {

    @Select("""
            <script>
            SELECT t.id, t.order_no, t.cargo_name, t.cargo_type, t.cargo_weight,
                   t.origin_port, t.destination_port, t.ship_id, s.ship_name,
                   t.customer_name, t.customer_phone,
                   t.planned_departure_time, t.planned_arrival_time,
                   t.actual_departure_time, t.actual_arrival_time,
                   t.status, st.id AS settlement_id,
                   COALESCE(st.status, 'UNSETTLED') AS settlement_status,
                   t.remark, t.created_at, t.updated_at
            FROM transport_order t
            LEFT JOIN ship s ON t.ship_id = s.id
            LEFT JOIN settlement st ON st.transport_order_id = t.id
            <where>
              <if test="keyword != null and keyword != ''">
                AND (t.order_no LIKE CONCAT('%', #{keyword}, '%')
                  OR t.cargo_name LIKE CONCAT('%', #{keyword}, '%')
                  OR t.customer_name LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="shipId != null">
                AND t.ship_id = #{shipId}
              </if>
              <if test="originPort != null and originPort != ''">
                AND t.origin_port = #{originPort}
              </if>
              <if test="destinationPort != null and destinationPort != ''">
                AND t.destination_port = #{destinationPort}
              </if>
              <if test="status != null">
                AND t.status = #{status}
              </if>
              <if test="startDate != null">
                AND DATE(t.planned_departure_time) &gt;= #{startDate}
              </if>
              <if test="endDate != null">
                AND DATE(t.planned_departure_time) &lt;= #{endDate}
              </if>
            </where>
            ORDER BY t.id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<TransportOrderEntity> findPage(
            @Param("keyword") String keyword,
            @Param("shipId") Long shipId,
            @Param("originPort") String originPort,
            @Param("destinationPort") String destinationPort,
            @Param("status") TransportStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM transport_order t
            <where>
              <if test="keyword != null and keyword != ''">
                AND (t.order_no LIKE CONCAT('%', #{keyword}, '%')
                  OR t.cargo_name LIKE CONCAT('%', #{keyword}, '%')
                  OR t.customer_name LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="shipId != null">
                AND t.ship_id = #{shipId}
              </if>
              <if test="originPort != null and originPort != ''">
                AND t.origin_port = #{originPort}
              </if>
              <if test="destinationPort != null and destinationPort != ''">
                AND t.destination_port = #{destinationPort}
              </if>
              <if test="status != null">
                AND t.status = #{status}
              </if>
              <if test="startDate != null">
                AND DATE(t.planned_departure_time) &gt;= #{startDate}
              </if>
              <if test="endDate != null">
                AND DATE(t.planned_departure_time) &lt;= #{endDate}
              </if>
            </where>
            </script>
            """)
    long count(
            @Param("keyword") String keyword,
            @Param("shipId") Long shipId,
            @Param("originPort") String originPort,
            @Param("destinationPort") String destinationPort,
            @Param("status") TransportStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Select("""
            SELECT t.id, t.order_no, t.cargo_name, t.cargo_type, t.cargo_weight,
                   t.origin_port, t.destination_port, t.ship_id, s.ship_name,
                   t.customer_name, t.customer_phone,
                   t.planned_departure_time, t.planned_arrival_time,
                   t.actual_departure_time, t.actual_arrival_time,
                   t.status, st.id AS settlement_id,
                   COALESCE(st.status, 'UNSETTLED') AS settlement_status,
                   t.remark, t.created_at, t.updated_at
            FROM transport_order t
            LEFT JOIN ship s ON t.ship_id = s.id
            LEFT JOIN settlement st ON st.transport_order_id = t.id
            WHERE t.id = #{id}
            """)
    Optional<TransportOrderEntity> findById(Long id);

    @Select("SELECT id, ship_name, load_capacity, status FROM ship WHERE id = #{id}")
    Optional<ShipOption> findShipById(Long id);

    @Select("SELECT COUNT(*) FROM transport_order WHERE order_no LIKE CONCAT(#{prefix}, '%')")
    int countByOrderNoPrefix(String prefix);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM transport_order
            WHERE ship_id = #{shipId}
              AND status IN ('PENDING', 'IN_TRANSIT')
              AND planned_departure_time &lt; #{plannedArrivalTime}
              AND planned_arrival_time &gt; #{plannedDepartureTime}
              <if test="excludeId != null">
                AND id != #{excludeId}
              </if>
            </script>
            """)
    int countScheduleConflicts(
            @Param("shipId") Long shipId,
            @Param("plannedDepartureTime") LocalDateTime plannedDepartureTime,
            @Param("plannedArrivalTime") LocalDateTime plannedArrivalTime,
            @Param("excludeId") Long excludeId
    );

    @Select("SELECT COUNT(*) FROM settlement WHERE transport_order_id = #{transportOrderId}")
    int countSettlementByTransportOrderId(Long transportOrderId);

    @Insert("""
            INSERT INTO transport_order (
              order_no, cargo_name, cargo_type, cargo_weight, origin_port, destination_port,
              ship_id, customer_name, customer_phone, planned_departure_time,
              planned_arrival_time, status, remark
            )
            VALUES (
              #{orderNo}, #{cargoName}, #{cargoType}, #{cargoWeight}, #{originPort}, #{destinationPort},
              #{shipId}, #{customerName}, #{customerPhone}, #{plannedDepartureTime},
              #{plannedArrivalTime}, #{status}, #{remark}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TransportOrderEntity transportOrder);

    @Update("""
            UPDATE transport_order
            SET cargo_name = #{cargoName},
                cargo_type = #{cargoType},
                cargo_weight = #{cargoWeight},
                origin_port = #{originPort},
                destination_port = #{destinationPort},
                ship_id = #{shipId},
                customer_name = #{customerName},
                customer_phone = #{customerPhone},
                planned_departure_time = #{plannedDepartureTime},
                planned_arrival_time = #{plannedArrivalTime},
                remark = #{remark}
            WHERE id = #{id}
            """)
    int update(TransportOrderEntity transportOrder);

    @Update("""
            UPDATE transport_order
            SET status = #{status},
                actual_departure_time = #{actualDepartureTime},
                actual_arrival_time = #{actualArrivalTime}
            WHERE id = #{id}
            """)
    int updateStatus(TransportOrderEntity transportOrder);

    @Delete("DELETE FROM transport_order WHERE id = #{id}")
    int deleteById(Long id);
}
