package com.self_true.controller;

import com.self_true.model.dto.request.PublicSceneRequest;
import com.self_true.model.dto.response.Response;
import com.self_true.service.PublicStoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final PublicStoryService publicStoryService;

    public AdminController(PublicStoryService publicStoryService) {
        this.publicStoryService = publicStoryService;
    }

    @Operation(summary = "public 장면 저장")
    @PostMapping("/scene")
    public ResponseEntity<Response> postStory(@RequestBody PublicSceneRequest request) {
        publicStoryService.save(request);
        return ResponseEntity.ok(new Response(true, "장면 저장 완료"));
    }
}
