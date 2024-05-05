package com.example;

import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.service.impl.AccountServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
class SpringBootDemoCs0BackengApplicationTests {

    @Resource
    AccountServiceImpl accountService;

    @Resource
    AccountMapper accountMapper;
    @Test
    void contextLoads() {
        log.info("csancakjcas   " + accountMapper.existxAccountByEmail("1845494196@qq.com"));
    }

}
