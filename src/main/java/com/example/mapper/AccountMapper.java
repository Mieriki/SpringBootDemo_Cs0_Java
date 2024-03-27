package com.example.mapper;

import com.example.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccountMapper {
    @Select("select * from db_Account where username = #{info} or password = #{info};")
    Account findAccountByUsernameOrEmail(String info);
}
