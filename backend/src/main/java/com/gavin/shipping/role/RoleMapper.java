package com.gavin.shipping.role;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMapper {

    @Select("""
            SELECT id, role_name, role_code, description, built_in
            FROM sys_role
            ORDER BY built_in DESC, id ASC
            """)
    List<RoleEntity> findAll();

    @Select("""
            SELECT id, role_name, role_code, description, built_in
            FROM sys_role
            WHERE id = #{id}
            """)
    Optional<RoleEntity> findById(Long id);

    @Select("SELECT COUNT(*) FROM sys_role WHERE role_code = #{roleCode}")
    int countByRoleCode(String roleCode);

    @Select("SELECT COUNT(*) FROM sys_user WHERE role_id = #{roleId}")
    int countUsersByRoleId(Long roleId);

    @Insert("""
            INSERT INTO sys_role (role_name, role_code, description, built_in)
            VALUES (#{roleName}, #{roleCode}, #{description}, #{builtIn})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RoleEntity role);

    @Update("""
            UPDATE sys_role
            SET role_name = #{roleName},
                description = #{description}
            WHERE id = #{id}
            """)
    int update(RoleEntity role);

    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int deleteById(Long id);
}
