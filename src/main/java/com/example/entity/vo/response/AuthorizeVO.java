package com.example.entity.vo.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder(toBuilder = true)
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}
