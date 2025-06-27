package com.self_true.controller;

import com.self_true.model.dto.request.PrivateSceneRequest;
import com.self_true.model.dto.response.PrivateStoryResponse;
import com.self_true.model.dto.response.Response;
import com.self_true.service.PrivateStoryService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/my")
@Tag(name = "MyController", description = "사용자 개인 스토리 API")
public class MyController {

    private final PrivateStoryService privateStoryService;

    public MyController(PrivateStoryService privateStoryService) {
        this.privateStoryService = privateStoryService;
    }

    @Operation(summary = "내 장면 저장")
    @PostMapping("/scene/{id}")
    public ResponseEntity<Response> createOrUpdate(@PathVariable String id,
                                                   @RequestBody PrivateSceneRequest request,
                                                   @AuthenticationPrincipal String memberId) {
        privateStoryService.createOrUpdate(request, id, memberId);
        return ResponseEntity.ok(new Response(true, "장면 저장 완료"));
    }

    @PostMapping("/scenes/bulk")
    public ResponseEntity<Response> saveBulk(@RequestBody List<PrivateSceneRequest> requests,
                                             @AuthenticationPrincipal String memberId) {
        privateStoryService.saveAll(requests, memberId);
        return ResponseEntity.ok(new Response(true, "벌크 장면 저장 완료"));
    }

    @Operation(summary = "내 장면 삭제")
    @DeleteMapping("/scene/{id}")
    public ResponseEntity<Response> delete(@PathVariable String id,
                                           @AuthenticationPrincipal String memberId) {
        privateStoryService.delete(id, memberId);
        return ResponseEntity.ok(new Response(true, "장면 삭제 완료"));
    }

    @Operation(summary = "내 장면 전체 조회")
    @GetMapping("/story")
    public ResponseEntity<PrivateStoryResponse> getStory(@AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(privateStoryService.getPrivateScenes(memberId));
    }

    @Operation(summary = "내 첫 장면 호출")
    @GetMapping("/scene")
    public ResponseEntity<?> getFirst(@AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(privateStoryService.getFirstScene(memberId));
    }

    @Operation(summary = "내 특정 장면 호출")
    @GetMapping("/scene/{id}")
    public ResponseEntity<?> getScene(@PathVariable String id, @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(privateStoryService.getPrivateScene(id, memberId));
    }
}
