package com.self_true.controller;

import com.self_true.service.PublicStoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
