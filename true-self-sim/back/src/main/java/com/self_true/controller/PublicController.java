package com.self_true.controller;

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

    @GetMapping("/story")
    public ResponseEntity<?> getStory() {
        // todo 전체 스토리 한 번에 가져가는 것보다, 첫 번째 장면부터 다음 장면을 가져가는게 log 저장하기에도 용이하지 않을까?
        return ResponseEntity.ok(publicStoryService.getPublicScenes());
    }

    @PostMapping("/scene")
    public ResponseEntity<Response> postStory(@RequestBody PublicSceneRequest request) {
        publicStoryService.save(request);
        return ResponseEntity.ok(new Response(true, "장면 저장 완료"));
    }
}
