package com.example.entity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class Account {
    Integer id;
    String username;
    String password;
    String email;
    String role;
    Date registerTime;
}
