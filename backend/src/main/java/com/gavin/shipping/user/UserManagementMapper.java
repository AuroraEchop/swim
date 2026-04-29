package com.gavin.shipping.user;

import com.gavin.shipping.domain.UserStatus;
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
public interface UserManagementMapper {

    @Select("""
            <script>
            SELECT u.id, u.username, u.password, u.real_name, u.phone, u.email,
                   u.role_id, r.role_name, r.role_code, u.status, u.created_at, u.updated_at
            FROM sys_user u
            LEFT JOIN sys_role r ON u.role_id = r.id
            <where>
              <if test="username != null and username != ''">
                AND u.username LIKE CONCAT('%', #{username}, '%')
              </if>
              <if test="realName != null and realName != ''">
                AND u.real_name LIKE CONCAT('%', #{realName}, '%')
              </if>
              <if test="roleId != null">
                AND u.role_id = #{roleId}
              </if>
              <if test="status != null">
                AND u.status = #{status}
              </if>
            </where>
            ORDER BY u.id ASC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<UserEntity> findPage(
            @Param("username") String username,
            @Param("realName") String realName,
            @Param("roleId") Long roleId,
            @Param("status") UserStatus status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM sys_user u
            <where>
              <if test="username != null and username != ''">
                AND u.username LIKE CONCAT('%', #{username}, '%')
              </if>
              <if test="realName != null and realName != ''">
                AND u.real_name LIKE CONCAT('%', #{realName}, '%')
              </if>
              <if test="roleId != null">
                AND u.role_id = #{roleId}
              </if>
              <if test="status != null">
                AND u.status = #{status}
              </if>
            </where>
            </script>
            """)
    long count(
            @Param("username") String username,
            @Param("realName") String realName,
            @Param("roleId") Long roleId,
            @Param("status") UserStatus status
    );

    @Select("""
            SELECT u.id, u.username, u.password, u.real_name, u.phone, u.email,
                   u.role_id, r.role_name, r.role_code, u.status, u.created_at, u.updated_at
            FROM sys_user u
            LEFT JOIN sys_role r ON u.role_id = r.id
            WHERE u.id = #{id}
            """)
    Optional<UserEntity> findById(Long id);

    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    int countByUsername(String username);

    @Select("SELECT COUNT(*) FROM sys_role WHERE id = #{roleId}")
    int countRoleById(Long roleId);

    @Insert("""
            INSERT INTO sys_user (username, password, real_name, phone, email, role_id, status)
            VALUES (#{username}, #{password}, #{realName}, #{phone}, #{email}, #{roleId}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserEntity user);

    @Update("""
            UPDATE sys_user
            SET real_name = #{realName},
                phone = #{phone},
                email = #{email},
                role_id = #{roleId},
                status = #{status}
            WHERE id = #{id}
            """)
    int update(UserEntity user);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(Long id);
}
