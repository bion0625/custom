package com.self_true.controller;

import com.self_true.model.dto.request.PublicChoiceRequest;
import com.self_true.service.PublicStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/public")
@RestController
@Tag(name = "PublicController", description = "공개 컨텐츠 관련 API")
public class PublicController {

    private final PublicStoryService publicStoryService;

    public PublicController(PublicStoryService publicStoryService) {
        this.publicStoryService = publicStoryService;
    }

    @Operation(summary = "(다음) 장면 호출", description = "nextSceneId is null 이면 첫 장면 호출")
    @GetMapping("/scene")
    public ResponseEntity<?> getScene(PublicChoiceRequest request, @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(publicStoryService.getPublicScene(request, memberId));
    }
}
