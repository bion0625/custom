package com.self_true.controller;

import com.self_true.model.dto.request.PrivateSceneRequest;
import com.self_true.model.dto.request.PrivateStoryRequest;
import com.self_true.model.dto.response.PrivateStoryResponse;
import com.self_true.model.dto.response.Response;
import com.self_true.service.PrivateStoryService;
import com.self_true.model.entity.PrivateStory;
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

    @Operation(summary = "내 스토리 목록 조회")
    @GetMapping("/stories")
    public ResponseEntity<?> getStories(@AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(privateStoryService.getStories(memberId));
    }

    @Operation(summary = "새 스토리 생성")
    @PostMapping("/stories")
    public ResponseEntity<PrivateStory> createStory(@RequestBody PrivateStoryRequest request,
                                                   @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(privateStoryService.createStory(request.getTitle(), memberId));
    }

    @Operation(summary = "내 스토리 삭제")
    @DeleteMapping("/stories/{storyId}")
    public ResponseEntity<Response> deleteStory(@PathVariable Long storyId,
                                                @AuthenticationPrincipal String memberId) {
        privateStoryService.deleteStory(storyId, memberId);
        return ResponseEntity.ok(new Response(true, "스토리 삭제 완료"));
    }

    @Operation(summary = "내 장면 저장")
    @PostMapping("/story/{storyId}/scene/{id}")
    public ResponseEntity<Response> createOrUpdate(@PathVariable String id,
                                                   @RequestBody PrivateSceneRequest request,
                                                   @PathVariable Long storyId,
                                                   @AuthenticationPrincipal String memberId) {
        privateStoryService.createOrUpdate(request, id, storyId, memberId);
        return ResponseEntity.ok(new Response(true, "장면 저장 완료"));
    }

    @PostMapping("/story/{storyId}/scenes/bulk")
    public ResponseEntity<Response> saveBulk(@RequestBody List<PrivateSceneRequest> requests,
                                             @PathVariable Long storyId,
                                             @AuthenticationPrincipal String memberId) {
        privateStoryService.saveAll(requests, storyId, memberId);
        return ResponseEntity.ok(new Response(true, "벌크 장면 저장 완료"));
    }

    @Operation(summary = "내 장면 삭제")
    @DeleteMapping("/story/{storyId}/scene/{id}")
    public ResponseEntity<Response> delete(@PathVariable String id,
                                           @PathVariable Long storyId,
                                           @AuthenticationPrincipal String memberId) {
        privateStoryService.delete(id, storyId, memberId);
        return ResponseEntity.ok(new Response(true, "장면 삭제 완료"));
    }

    @Operation(summary = "내 장면 전체 조회")
    @GetMapping("/story/{storyId}")
    public ResponseEntity<PrivateStoryResponse> getStory(@PathVariable Long storyId,
                                                         @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(privateStoryService.getPrivateScenes(storyId, memberId));
    }

    @Operation(summary = "내 첫 장면 호출")
    @GetMapping("/story/{storyId}/scene")
    public ResponseEntity<?> getFirst(@PathVariable Long storyId,
                                      @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(privateStoryService.getFirstScene(storyId, memberId));
    }

    @Operation(summary = "내 특정 장면 호출")
    @GetMapping("/story/{storyId}/scene/{id}")
    public ResponseEntity<?> getScene(@PathVariable String id,
                                      @PathVariable Long storyId,
                                      @AuthenticationPrincipal String memberId,
                                      @RequestParam(value = "choice", required = false) String choice) {
        return ResponseEntity.ok(privateStoryService.getPrivateScene(id, storyId, memberId, choice));
    }
}
