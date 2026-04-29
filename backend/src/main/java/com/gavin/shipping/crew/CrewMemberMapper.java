package com.gavin.shipping.crew;

import com.gavin.shipping.domain.CrewStatus;
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
public interface CrewMemberMapper {

    @Select("""
            <script>
            SELECT c.id, c.crew_no, c.name, c.gender, c.phone, c.certificate_no, c.position,
                   c.ship_id, s.ship_name, c.status, c.remark, c.created_at, c.updated_at
            FROM crew_member c
            LEFT JOIN ship s ON c.ship_id = s.id
            <where>
              <if test="keyword != null and keyword != ''">
                AND (c.name LIKE CONCAT('%', #{keyword}, '%')
                  OR c.crew_no LIKE CONCAT('%', #{keyword}, '%')
                  OR c.certificate_no LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="position != null and position != ''">
                AND c.position = #{position}
              </if>
              <if test="shipId != null">
                AND c.ship_id = #{shipId}
              </if>
              <if test="status != null">
                AND c.status = #{status}
              </if>
            </where>
            ORDER BY c.id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<CrewMemberEntity> findPage(
            @Param("keyword") String keyword,
            @Param("position") String position,
            @Param("shipId") Long shipId,
            @Param("status") CrewStatus status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM crew_member c
            <where>
              <if test="keyword != null and keyword != ''">
                AND (c.name LIKE CONCAT('%', #{keyword}, '%')
                  OR c.crew_no LIKE CONCAT('%', #{keyword}, '%')
                  OR c.certificate_no LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="position != null and position != ''">
                AND c.position = #{position}
              </if>
              <if test="shipId != null">
                AND c.ship_id = #{shipId}
              </if>
              <if test="status != null">
                AND c.status = #{status}
              </if>
            </where>
            </script>
            """)
    long count(
            @Param("keyword") String keyword,
            @Param("position") String position,
            @Param("shipId") Long shipId,
            @Param("status") CrewStatus status
    );

    @Select("""
            SELECT c.id, c.crew_no, c.name, c.gender, c.phone, c.certificate_no, c.position,
                   c.ship_id, s.ship_name, c.status, c.remark, c.created_at, c.updated_at
            FROM crew_member c
            LEFT JOIN ship s ON c.ship_id = s.id
            WHERE c.id = #{id}
            """)
    Optional<CrewMemberEntity> findById(Long id);

    @Select("SELECT COUNT(*) FROM crew_member WHERE crew_no = #{crewNo}")
    int countByCrewNo(String crewNo);

    @Select("SELECT COUNT(*) FROM crew_member WHERE certificate_no = #{certificateNo}")
    int countByCertificateNo(String certificateNo);

    @Select("SELECT COUNT(*) FROM crew_member WHERE certificate_no = #{certificateNo} AND id != #{id}")
    int countByCertificateNoExcludingId(@Param("certificateNo") String certificateNo, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM ship WHERE id = #{shipId}")
    int countShipById(Long shipId);

    @Select("""
            SELECT COUNT(*)
            FROM transport_order
            WHERE ship_id = #{shipId}
              AND status IN ('PENDING', 'IN_TRANSIT')
            """)
    int countActiveTransportOrdersByShipId(Long shipId);

    @Insert("""
            INSERT INTO crew_member (crew_no, name, gender, phone, certificate_no, position, ship_id, status, remark)
            VALUES (#{crewNo}, #{name}, #{gender}, #{phone}, #{certificateNo}, #{position}, #{shipId}, #{status}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrewMemberEntity crewMember);

    @Update("""
            UPDATE crew_member
            SET name = #{name},
                gender = #{gender},
                phone = #{phone},
                certificate_no = #{certificateNo},
                position = #{position},
                ship_id = #{shipId},
                status = #{status},
                remark = #{remark}
            WHERE id = #{id}
            """)
    int update(CrewMemberEntity crewMember);

    @Update("UPDATE crew_member SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") CrewStatus status);

    @Delete("DELETE FROM crew_member WHERE id = #{id}")
    int deleteById(Long id);
}
