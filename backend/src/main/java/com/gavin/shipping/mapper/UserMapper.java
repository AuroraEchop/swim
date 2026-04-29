package com.gavin.shipping.mapper;

import com.gavin.shipping.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT
              u.id,
              u.username,
              u.password,
              u.real_name AS realName,
              r.role_code AS roleCode,
              u.status
            FROM sys_user u
            LEFT JOIN sys_role r ON u.role_id = r.id
            WHERE u.username = #{username}
            LIMIT 1
            """)
    Optional<UserAccount> findByUsername(String username);
}
