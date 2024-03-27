package com.example.service;

import com.example.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService {
    Account findAccountByUsernameOrEmail(String info);
}
