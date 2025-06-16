package com.example.demo.controller;

import com.example.demo.model.dto.Response;
import com.example.demo.model.dto.UsersRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "UserController", description = "User API")
public class UserController {

    @Operation(summary = "회원가입", description = "회원가입 요청을 처리")
    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody UsersRequest request) {
        return ResponseEntity.ok(new Response(true));
    }
}
