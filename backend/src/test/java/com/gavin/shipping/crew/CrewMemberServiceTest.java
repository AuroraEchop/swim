package com.gavin.shipping.crew;

import com.gavin.shipping.common.ConflictException;
import com.gavin.shipping.common.NotFoundException;
import com.gavin.shipping.common.PageResult;
import com.gavin.shipping.domain.CrewStatus;
import com.gavin.shipping.crew.dto.CreateCrewMemberRequest;
import com.gavin.shipping.crew.dto.CrewMemberCreateResponse;
import com.gavin.shipping.crew.dto.CrewMemberResponse;
import com.gavin.shipping.crew.dto.UpdateCrewMemberRequest;
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
class CrewMemberServiceTest {

    @Mock
    private CrewMemberMapper crewMemberMapper;

    @InjectMocks
    private CrewMemberService crewMemberService;

    @Test
    void shouldReturnPagedCrewMembers() {
        when(crewMemberMapper.count("张", "船长", 1L, CrewStatus.ON_DUTY)).thenReturn(1L);
        when(crewMemberMapper.findPage("张", "船长", 1L, CrewStatus.ON_DUTY, 0, 10))
                .thenReturn(List.of(crewMemberEntity()));

        PageResult<CrewMemberResponse> result = crewMemberService.findPage("张", "船长", 1L, CrewStatus.ON_DUTY, 1, 10);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).name()).isEqualTo("张三");
        assertThat(result.records().get(0).shipName()).isEqualTo("远航一号");
    }

    @Test
    void shouldCreateCrewMember() {
        when(crewMemberMapper.countByCrewNo("CREW-003")).thenReturn(0);
        when(crewMemberMapper.countByCertificateNo("CERT-003")).thenReturn(0);
        when(crewMemberMapper.countShipById(1L)).thenReturn(1);
        doAnswer(invocation -> {
            CrewMemberEntity crewMember = invocation.getArgument(0);
            crewMember.setId(3L);
            return 1;
        }).when(crewMemberMapper).insert(any(CrewMemberEntity.class));

        CrewMemberCreateResponse response = crewMemberService.create(createRequest());

        assertThat(response.id()).isEqualTo(3L);
        ArgumentCaptor<CrewMemberEntity> captor = ArgumentCaptor.forClass(CrewMemberEntity.class);
        verify(crewMemberMapper).insert(captor.capture());
        assertThat(captor.getValue().getCrewNo()).isEqualTo("CREW-003");
        assertThat(captor.getValue().getStatus()).isEqualTo(CrewStatus.ON_DUTY);
    }

    @Test
    void shouldRejectDuplicatedCrewNo() {
        when(crewMemberMapper.countByCrewNo("CREW-003")).thenReturn(1);

        assertThatThrownBy(() -> crewMemberService.create(createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("船员编号已存在");
        verify(crewMemberMapper, never()).insert(any());
    }

    @Test
    void shouldRejectDuplicatedCertificateNo() {
        when(crewMemberMapper.countByCrewNo("CREW-003")).thenReturn(0);
        when(crewMemberMapper.countByCertificateNo("CERT-003")).thenReturn(1);

        assertThatThrownBy(() -> crewMemberService.create(createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("证件编号已存在");
        verify(crewMemberMapper, never()).insert(any());
    }

    @Test
    void shouldRejectMissingShip() {
        when(crewMemberMapper.countByCrewNo("CREW-003")).thenReturn(0);
        when(crewMemberMapper.countByCertificateNo("CERT-003")).thenReturn(0);
        when(crewMemberMapper.countShipById(1L)).thenReturn(0);

        assertThatThrownBy(() -> crewMemberService.create(createRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("所属船舶不存在");
    }

    @Test
    void shouldUpdateCrewMember() {
        when(crewMemberMapper.findById(1L)).thenReturn(Optional.of(crewMemberEntity()));
        when(crewMemberMapper.countByCertificateNoExcludingId("CERT-003", 1L)).thenReturn(0);
        when(crewMemberMapper.countShipById(1L)).thenReturn(1);

        crewMemberService.update(1L, new UpdateCrewMemberRequest(
                "张三",
                "男",
                "13800000002",
                "CERT-003",
                "大副",
                1L,
                CrewStatus.ON_DUTY,
                "岗位调整"
        ));

        ArgumentCaptor<CrewMemberEntity> captor = ArgumentCaptor.forClass(CrewMemberEntity.class);
        verify(crewMemberMapper).update(captor.capture());
        assertThat(captor.getValue().getCertificateNo()).isEqualTo("CERT-003");
        assertThat(captor.getValue().getPosition()).isEqualTo("大副");
    }

    @Test
    void shouldRejectDeleteWhenCrewMemberOnDutyAndShipHasActiveTransportOrders() {
        when(crewMemberMapper.findById(1L)).thenReturn(Optional.of(crewMemberEntity()));
        when(crewMemberMapper.countActiveTransportOrdersByShipId(1L)).thenReturn(1);

        assertThatThrownBy(() -> crewMemberService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("当前在岗且所属船舶正在运输中的船员不能删除");
        verify(crewMemberMapper, never()).deleteById(1L);
    }

    private CreateCrewMemberRequest createRequest() {
        return new CreateCrewMemberRequest(
                "CREW-003",
                "王五",
                "男",
                "13800000004",
                "CERT-003",
                "轮机长",
                1L,
                CrewStatus.ON_DUTY,
                "新增船员"
        );
    }

    private CrewMemberEntity crewMemberEntity() {
        CrewMemberEntity crewMember = new CrewMemberEntity();
        crewMember.setId(1L);
        crewMember.setCrewNo("CREW-001");
        crewMember.setName("张三");
        crewMember.setGender("男");
        crewMember.setPhone("13800000002");
        crewMember.setCertificateNo("CERT-001");
        crewMember.setPosition("船长");
        crewMember.setShipId(1L);
        crewMember.setShipName("远航一号");
        crewMember.setStatus(CrewStatus.ON_DUTY);
        crewMember.setRemark("经验丰富");
        crewMember.setCreatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        crewMember.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 10, 0));
        return crewMember;
    }
}
