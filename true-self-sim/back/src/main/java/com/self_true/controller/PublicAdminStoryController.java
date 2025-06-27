package com.self_true.controller;

import com.self_true.model.dto.response.AdminStoriesResponse;
import com.self_true.model.dto.response.AdminStoryInfo;
import com.self_true.service.PrivateStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/admin")
@Tag(name = "PublicAdminStoryController", description = "관리자 스토리 공개 API")
public class PublicAdminStoryController {

    private final PrivateStoryService privateStoryService;

    public PublicAdminStoryController(PrivateStoryService privateStoryService) {
        this.privateStoryService = privateStoryService;
    }

    @Operation(summary = "관리자 스토리 목록 조회")
    @GetMapping("/stories")
    public ResponseEntity<AdminStoriesResponse> getStories() {
        List<AdminStoryInfo> infos = privateStoryService.getAdminStories();
        return ResponseEntity.ok(new AdminStoriesResponse(infos));
    }
}
