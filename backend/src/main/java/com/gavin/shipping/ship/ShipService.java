package com.gavin.shipping.ship;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.ShipStatus;
import com.gavin.shipping.ship.dto.CreateShipRequest;
import com.gavin.shipping.ship.dto.ShipCreateResponse;
import com.gavin.shipping.ship.dto.ShipResponse;
import com.gavin.shipping.ship.dto.UpdateShipRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipService {

    private final ShipMapper shipMapper;

    public ShipService(ShipMapper shipMapper) {
        this.shipMapper = shipMapper;
    }

    public PageResult<ShipResponse> findPage(
            String keyword,
            String type,
            String homePort,
            ShipStatus status,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (safePage - 1) * safePageSize;
        long total = shipMapper.count(keyword, type, homePort, status);
        List<ShipResponse> records = shipMapper.findPage(keyword, type, homePort, status, offset, safePageSize)
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResult.of(records, safePage, safePageSize, total);
    }

    public ShipResponse findById(Long id) {
        return shipMapper.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("船舶不存在"));
    }

    public ShipCreateResponse create(CreateShipRequest request) {
        if (shipMapper.countByShipNo(request.shipNo()) > 0) {
            throw new ConflictException("船舶编号已存在");
        }
        ShipEntity ship = new ShipEntity();
        ship.setShipNo(request.shipNo());
        ship.setShipName(request.shipName());
        ship.setShipType(request.shipType());
        ship.setLoadCapacity(request.loadCapacity());
        ship.setHomePort(request.homePort());
        ship.setStatus(request.status());
        ship.setRemark(request.remark());
        shipMapper.insert(ship);
        return new ShipCreateResponse(ship.getId());
    }

    public void update(Long id, UpdateShipRequest request) {
        ShipEntity ship = shipMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("船舶不存在"));
        ship.setShipName(request.shipName());
        ship.setShipType(request.shipType());
        ship.setLoadCapacity(request.loadCapacity());
        ship.setHomePort(request.homePort());
        ship.setStatus(request.status());
        ship.setRemark(request.remark());
        shipMapper.update(ship);
    }

    public void updateStatus(Long id, ShipStatus status) {
        if (shipMapper.findById(id).isEmpty()) {
            throw new NotFoundException("船舶不存在");
        }
        shipMapper.updateStatus(id, status);
    }

    public void delete(Long id) {
        if (shipMapper.findById(id).isEmpty()) {
            throw new NotFoundException("船舶不存在");
        }
        if (shipMapper.countActiveTransportOrders(id) > 0) {
            throw new ConflictException("正在执行运输任务的船舶不能删除");
        }
        shipMapper.deleteById(id);
    }

    private ShipResponse toResponse(ShipEntity ship) {
        return new ShipResponse(
                ship.getId(),
                ship.getShipNo(),
                ship.getShipName(),
                ship.getShipType(),
                ship.getLoadCapacity(),
                ship.getHomePort(),
                ship.getStatus(),
                ship.getRemark(),
                ship.getCreatedAt(),
                ship.getUpdatedAt()
        );
    }
}
