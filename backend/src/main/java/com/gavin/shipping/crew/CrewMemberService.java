package com.gavin.shipping.crew;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.CrewStatus;
import com.gavin.shipping.crew.dto.CreateCrewMemberRequest;
import com.gavin.shipping.crew.dto.CrewMemberCreateResponse;
import com.gavin.shipping.crew.dto.CrewMemberResponse;
import com.gavin.shipping.crew.dto.UpdateCrewMemberRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrewMemberService {

    private final CrewMemberMapper crewMemberMapper;

    public CrewMemberService(CrewMemberMapper crewMemberMapper) {
        this.crewMemberMapper = crewMemberMapper;
    }

    public PageResult<CrewMemberResponse> findPage(
            String keyword,
            String position,
            Long shipId,
            CrewStatus status,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (safePage - 1) * safePageSize;
        long total = crewMemberMapper.count(keyword, position, shipId, status);
        List<CrewMemberResponse> records = crewMemberMapper.findPage(keyword, position, shipId, status, offset, safePageSize)
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResult.of(records, safePage, safePageSize, total);
    }

    public CrewMemberResponse findById(Long id) {
        return crewMemberMapper.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("船员不存在"));
    }

    public CrewMemberCreateResponse create(CreateCrewMemberRequest request) {
        if (crewMemberMapper.countByCrewNo(request.crewNo()) > 0) {
            throw new ConflictException("船员编号已存在");
        }
        if (crewMemberMapper.countByCertificateNo(request.certificateNo()) > 0) {
            throw new ConflictException("证件编号已存在");
        }
        validateShipExists(request.shipId());

        CrewMemberEntity crewMember = new CrewMemberEntity();
        crewMember.setCrewNo(request.crewNo());
        crewMember.setName(request.name());
        crewMember.setGender(request.gender());
        crewMember.setPhone(request.phone());
        crewMember.setCertificateNo(request.certificateNo());
        crewMember.setPosition(request.position());
        crewMember.setShipId(request.shipId());
        crewMember.setStatus(request.status());
        crewMember.setRemark(request.remark());
        crewMemberMapper.insert(crewMember);
        return new CrewMemberCreateResponse(crewMember.getId());
    }

    public void update(Long id, UpdateCrewMemberRequest request) {
        CrewMemberEntity crewMember = crewMemberMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("船员不存在"));
        if (crewMemberMapper.countByCertificateNoExcludingId(request.certificateNo(), id) > 0) {
            throw new ConflictException("证件编号已存在");
        }
        validateShipExists(request.shipId());

        crewMember.setName(request.name());
        crewMember.setGender(request.gender());
        crewMember.setPhone(request.phone());
        crewMember.setCertificateNo(request.certificateNo());
        crewMember.setPosition(request.position());
        crewMember.setShipId(request.shipId());
        crewMember.setStatus(request.status());
        crewMember.setRemark(request.remark());
        crewMemberMapper.update(crewMember);
    }

    public void updateStatus(Long id, CrewStatus status) {
        if (crewMemberMapper.findById(id).isEmpty()) {
            throw new NotFoundException("船员不存在");
        }
        crewMemberMapper.updateStatus(id, status);
    }

    public void delete(Long id) {
        CrewMemberEntity crewMember = crewMemberMapper.findById(id)
                .orElseThrow(() -> new NotFoundException("船员不存在"));
        if (crewMember.getStatus() == CrewStatus.ON_DUTY
                && crewMember.getShipId() != null
                && crewMemberMapper.countActiveTransportOrdersByShipId(crewMember.getShipId()) > 0) {
            throw new ConflictException("当前在岗且所属船舶正在运输中的船员不能删除");
        }
        crewMemberMapper.deleteById(id);
    }

    private void validateShipExists(Long shipId) {
        if (shipId != null && crewMemberMapper.countShipById(shipId) == 0) {
            throw new NotFoundException("所属船舶不存在");
        }
    }

    private CrewMemberResponse toResponse(CrewMemberEntity crewMember) {
        return new CrewMemberResponse(
                crewMember.getId(),
                crewMember.getCrewNo(),
                crewMember.getName(),
                crewMember.getGender(),
                crewMember.getPhone(),
                crewMember.getCertificateNo(),
                crewMember.getPosition(),
                crewMember.getShipId(),
                crewMember.getShipName(),
                crewMember.getStatus(),
                crewMember.getRemark(),
                crewMember.getCreatedAt(),
                crewMember.getUpdatedAt()
        );
    }
}
