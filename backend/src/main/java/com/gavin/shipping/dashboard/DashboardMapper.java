package com.gavin.shipping.dashboard;

import com.gavin.shipping.dashboard.dto.RecentTransportOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM ship")
    long countShips();

    @Select("SELECT COUNT(*) FROM crew_member")
    long countCrewMembers();

    @Select("SELECT COUNT(*) FROM transport_order WHERE status = 'PENDING'")
    long countPendingTransportOrders();

    @Select("SELECT COUNT(*) FROM transport_order WHERE status = 'IN_TRANSIT'")
    long countInTransitTransportOrders();

    @Select("SELECT COUNT(*) FROM settlement WHERE status IN ('UNSETTLED', 'PARTIAL')")
    long countUnsettledSettlements();

    @Select("SELECT COALESCE(SUM(receivable_amount), 0) FROM settlement")
    BigDecimal sumReceivableAmount();

    @Select("SELECT COALESCE(SUM(received_amount), 0) FROM settlement")
    BigDecimal sumReceivedAmount();

    @Select("""
            SELECT t.id, t.order_no, t.cargo_name, s.ship_name,
                   t.origin_port, t.destination_port, t.status, t.planned_departure_time
            FROM transport_order t
            LEFT JOIN ship s ON t.ship_id = s.id
            ORDER BY t.planned_departure_time DESC, t.id DESC
            LIMIT #{limit}
            """)
    List<RecentTransportOrderResponse> findRecentTransportOrders(int limit);
}
