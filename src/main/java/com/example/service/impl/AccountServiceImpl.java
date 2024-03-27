package com.example.service.impl;

import com.example.entity.dto.Account;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.HostHolder;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {
    @Resource
    AccountMapper mapper;

    @Resource
    HostHolder hostHolder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByUsernameOrEmail(username);
        if (account == null) throw new UsernameNotFoundException("用户名或密码错误");
        hostHolder.setAccount(account);
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    public Account findAccountByUsernameOrEmail(String info) {
        return mapper.findAccountByUsernameOrEmail(info);
    }
}
