package com.self_true.controller;

import com.self_true.model.dto.PublicSceneRequest;
import com.self_true.model.dto.Response;
import com.self_true.service.PublicStoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/public")
@RestController
public class PublicController {

    private final PublicStoryService publicStoryService;

    public PublicController(PublicStoryService publicStoryService) {
        this.publicStoryService = publicStoryService;
    }

    @GetMapping("/story")
    public ResponseEntity<?> getStory() {
        return ResponseEntity.ok(publicStoryService.getPublicScenes());
    }

    @PostMapping("/scene")
    public ResponseEntity<Response> postStory(@RequestBody PublicSceneRequest request) {
        publicStoryService.save(request);
        return ResponseEntity.ok(new Response(true, "장면 저장 완료"));
    }
}
