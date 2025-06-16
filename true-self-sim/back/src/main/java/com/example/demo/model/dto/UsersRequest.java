package com.example.demo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "회원가입 요청 DTO")
@Data
public class UsersRequest {
    private String id;
    private String password;
    private String name;
    private String email;
    private String phoneNumber;
}
