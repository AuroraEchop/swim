package com.gavin.shipping.settlement;

import com.gavin.shipping.domain.SettlementStatus;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SettlementMapper {

    @Select("""
            <script>
            SELECT st.id, st.settlement_no, st.transport_order_id, t.order_no AS transport_order_no,
                   st.customer_name, st.freight_amount, st.additional_fee, st.discount_amount,
                   st.receivable_amount, st.received_amount, st.status, st.settled_at,
                   st.remark, st.created_at, st.updated_at
            FROM settlement st
            LEFT JOIN transport_order t ON st.transport_order_id = t.id
            <where>
              <if test="keyword != null and keyword != ''">
                AND (st.settlement_no LIKE CONCAT('%', #{keyword}, '%')
                  OR t.order_no LIKE CONCAT('%', #{keyword}, '%')
                  OR st.customer_name LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="transportOrderId != null">
                AND st.transport_order_id = #{transportOrderId}
              </if>
              <if test="customerName != null and customerName != ''">
                AND st.customer_name LIKE CONCAT('%', #{customerName}, '%')
              </if>
              <if test="status != null">
                AND st.status = #{status}
              </if>
              <if test="startDate != null">
                AND DATE(st.created_at) &gt;= #{startDate}
              </if>
              <if test="endDate != null">
                AND DATE(st.created_at) &lt;= #{endDate}
              </if>
            </where>
            ORDER BY st.id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<SettlementEntity> findPage(
            @Param("keyword") String keyword,
            @Param("transportOrderId") Long transportOrderId,
            @Param("customerName") String customerName,
            @Param("status") SettlementStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM settlement st
            LEFT JOIN transport_order t ON st.transport_order_id = t.id
            <where>
              <if test="keyword != null and keyword != ''">
                AND (st.settlement_no LIKE CONCAT('%', #{keyword}, '%')
                  OR t.order_no LIKE CONCAT('%', #{keyword}, '%')
                  OR st.customer_name LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="transportOrderId != null">
                AND st.transport_order_id = #{transportOrderId}
              </if>
              <if test="customerName != null and customerName != ''">
                AND st.customer_name LIKE CONCAT('%', #{customerName}, '%')
              </if>
              <if test="status != null">
                AND st.status = #{status}
              </if>
              <if test="startDate != null">
                AND DATE(st.created_at) &gt;= #{startDate}
              </if>
              <if test="endDate != null">
                AND DATE(st.created_at) &lt;= #{endDate}
              </if>
            </where>
            </script>
            """)
    long count(
            @Param("keyword") String keyword,
            @Param("transportOrderId") Long transportOrderId,
            @Param("customerName") String customerName,
            @Param("status") SettlementStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Select("""
            SELECT st.id, st.settlement_no, st.transport_order_id, t.order_no AS transport_order_no,
                   st.customer_name, st.freight_amount, st.additional_fee, st.discount_amount,
                   st.receivable_amount, st.received_amount, st.status, st.settled_at,
                   st.remark, st.created_at, st.updated_at
            FROM settlement st
            LEFT JOIN transport_order t ON st.transport_order_id = t.id
            WHERE st.id = #{id}
            """)
    Optional<SettlementEntity> findById(Long id);

    @Select("SELECT id, order_no, customer_name FROM transport_order WHERE id = #{id}")
    Optional<TransportOrderSummary> findTransportOrderById(Long id);

    @Select("SELECT COUNT(*) FROM settlement WHERE transport_order_id = #{transportOrderId}")
    int countByTransportOrderId(Long transportOrderId);

    @Select("SELECT COUNT(*) FROM settlement WHERE settlement_no LIKE CONCAT(#{prefix}, '%')")
    int countBySettlementNoPrefix(String prefix);

    @Insert("""
            INSERT INTO settlement (
              settlement_no, transport_order_id, customer_name, freight_amount,
              additional_fee, discount_amount, receivable_amount, received_amount,
              status, settled_at, remark
            )
            VALUES (
              #{settlementNo}, #{transportOrderId}, #{customerName}, #{freightAmount},
              #{additionalFee}, #{discountAmount}, #{receivableAmount}, #{receivedAmount},
              #{status}, #{settledAt}, #{remark}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SettlementEntity settlement);

    @Update("""
            UPDATE settlement
            SET freight_amount = #{freightAmount},
                additional_fee = #{additionalFee},
                discount_amount = #{discountAmount},
                receivable_amount = #{receivableAmount},
                received_amount = #{receivedAmount},
                status = #{status},
                settled_at = #{settledAt},
                remark = #{remark}
            WHERE id = #{id}
            """)
    int update(SettlementEntity settlement);

    @Update("""
            UPDATE settlement
            SET received_amount = #{receivedAmount},
                status = #{status},
                settled_at = #{settledAt}
            WHERE id = #{id}
            """)
    int updatePayment(SettlementEntity settlement);

    @Delete("DELETE FROM settlement WHERE id = #{id}")
    int deleteById(Long id);
}
