package com.self_true.controller;

import com.self_true.model.dto.request.PublicChoiceRequest;
import com.self_true.model.dto.request.PublicSceneRequest;
import com.self_true.model.dto.response.Response;
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

    @GetMapping("/scene")
    public ResponseEntity<?> getScene(PublicChoiceRequest request) {
        return ResponseEntity.ok(publicStoryService.getPublicScene(request));
    }

    @PostMapping("/scene")
    public ResponseEntity<Response> postStory(@RequestBody PublicSceneRequest request) {
        publicStoryService.save(request);
        return ResponseEntity.ok(new Response(true, "장면 저장 완료"));
    }
}
