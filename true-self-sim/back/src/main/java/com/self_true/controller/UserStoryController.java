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

    @Operation(summary = "사용자 스토리 목록 조회")
    @GetMapping("/{memberId}/stories")
    public ResponseEntity<?> getStories(@PathVariable String memberId) {
        return ResponseEntity.ok(privateStoryService.getStories(memberId));
    }

    @Operation(summary = "사용자 첫 장면 호출")
    @GetMapping("/{memberId}/story/{storyId}/scene")
    public ResponseEntity<?> getFirst(@PathVariable String memberId,
                                      @PathVariable Long storyId,
                                      @AuthenticationPrincipal String viewerId) {
        return ResponseEntity.ok(privateStoryService.getFirstScene(storyId, memberId, viewerId));
    }

    @Operation(summary = "사용자 특정 장면 호출")
    @GetMapping("/{memberId}/story/{storyId}/scene/{id}")
    public ResponseEntity<?> getScene(@PathVariable String memberId, @PathVariable Long storyId,
                                      @PathVariable String id,
                                      @AuthenticationPrincipal String viewerId) {
        return ResponseEntity.ok(privateStoryService.getPrivateScene(id, storyId, memberId, viewerId));
    }
}
