package com.self_true.controller;

import com.self_true.service.PrivateStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "UserStoryController", description = "다른 사용자 개인 스토리 API")
public class UserStoryController {

    private final PrivateStoryService privateStoryService;

    public UserStoryController(PrivateStoryService privateStoryService) {
        this.privateStoryService = privateStoryService;
    }

    @Operation(summary = "사용자 첫 장면 호출")
    @GetMapping("/{memberId}/scene")
    public ResponseEntity<?> getFirst(@PathVariable String memberId,
                                      @AuthenticationPrincipal String viewerId) {
        return ResponseEntity.ok(privateStoryService.getFirstScene(memberId, viewerId));
    }

    @Operation(summary = "사용자 특정 장면 호출")
    @GetMapping("/{memberId}/scene/{id}")
    public ResponseEntity<?> getScene(@PathVariable String memberId, @PathVariable String id,
                                      @AuthenticationPrincipal String viewerId) {
        return ResponseEntity.ok(privateStoryService.getPrivateScene(id, memberId, viewerId));
    }
}
