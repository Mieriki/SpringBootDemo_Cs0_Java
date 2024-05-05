package com.example.entity.dto;

import com.example.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Account implements BaseData {
    Integer id;
    String username;
    String password;
    String email;
    String role;
    Date registerTime;
}
