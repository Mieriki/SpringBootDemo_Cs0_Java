package com.example.mapper;

import com.example.entity.dto.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper {
    @Select("select * from db_Account where username = #{info} or email = #{info};")
    Account findAccountByUsernameOrEmail(String info);

    @Select("select COUNT(email) from db_account where email = #{email};")
    boolean existxAccountByEmail(String email);

    @Select("select COUNT(username) from db_account where username = #{username};")
    boolean existxAccountByUsername(String username);

    @Insert("insert into db_Account (username, password, email, role, register_time) values (#{username}, #{password}, #{email}, #{role}, #{registerTime});")
    boolean addAccount(Account account);

    @Update("update db_Account set password = #{password} where email = #{email};")
    boolean resetPasswordByEmail(Account account);
}
