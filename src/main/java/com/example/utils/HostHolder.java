package com.example.utils;

import com.example.entity.dto.Account;
import org.springframework.stereotype.Component;

/**
 *持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {

    //ThreadLocal本质是以线程为key存储元素
    private ThreadLocal<Account> local = new ThreadLocal<>();

    public void setAccount(Account account){
        local.set(account);
    }

    public Account getAccount(){
        return local.get();
    }

    public void clear(){
        local.remove();
    }
}
