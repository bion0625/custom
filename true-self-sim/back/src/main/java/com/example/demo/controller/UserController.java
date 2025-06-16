package com.example.demo.controller;

import com.example.demo.model.dto.Response;
import com.example.demo.model.dto.UsersRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody UsersRequest request) {
        return ResponseEntity.ok(new Response(true));
    }
}
