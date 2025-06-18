package com.self_true.controller;

import com.self_true.model.dto.request.PublicSceneRequest;
import com.self_true.model.dto.response.PublicStoryResponse;
import com.self_true.model.dto.response.Response;
import com.self_true.service.PublicStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "AdminController", description = "관리자 API")
public class AdminController {

    private final PublicStoryService publicStoryService;

    public AdminController(PublicStoryService publicStoryService) {
        this.publicStoryService = publicStoryService;
    }

    @Operation(summary = "public 장면 저장")
    @PostMapping("/public/scene")
    public ResponseEntity<Response> postScene(@RequestBody PublicSceneRequest request) {
        publicStoryService.save(request);
        return ResponseEntity.ok(new Response(true, "장면 저장 완료"));
    }

    @Operation(summary = "public 장면 모두 불러오기", description = "start 플래그는 하나만 있어야 하므로, 전체 불러와서 비교 및 편집 예정")
    @GetMapping("/public/story")
    public ResponseEntity<PublicStoryResponse> getStory() {
        return ResponseEntity.ok(publicStoryService.getPublicScenes());
    }

    @Operation(summary = "public 장면 수정")
    @PutMapping("/public/scene/{id}")
    public ResponseEntity<Response> putScene(@PathVariable Long id, @RequestBody PublicSceneRequest request) {
        publicStoryService.update(request, id);
        return ResponseEntity.ok(new Response(true, "장면 저장 완료"));
    }
}
