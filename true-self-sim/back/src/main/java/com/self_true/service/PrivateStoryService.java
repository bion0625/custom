package com.self_true.service;

import com.self_true.exception.NotFoundSceneException;
import com.self_true.model.dto.request.PrivateSceneRequest;
import com.self_true.model.dto.response.AdminStoryInfo;
import com.self_true.model.dto.response.PrivateChoiceResponse;
import com.self_true.model.dto.response.PrivateSceneResponse;
import com.self_true.model.dto.response.PrivateStoryResponse;
import com.self_true.model.entity.Member;
import com.self_true.model.entity.PrivateChoice;
import com.self_true.model.entity.PrivateLog;
import com.self_true.model.entity.PrivateScene;
import com.self_true.repository.PrivateChoiceRepository;
import com.self_true.repository.PrivateLogRepository;
import com.self_true.repository.PrivateSceneRepository;
import com.self_true.repository.PrivateStoryRepository;
import com.self_true.model.entity.PrivateStory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrivateStoryService {
    private final PrivateSceneRepository sceneRepository;
    private final PrivateChoiceRepository choiceRepository;
    private final PrivateLogRepository logRepository;
    private final PrivateStoryRepository storyRepository;
    private final MemberService memberService;

    public PrivateStoryService(
            PrivateSceneRepository sceneRepository,
            PrivateChoiceRepository choiceRepository,
            PrivateLogRepository logRepository,
            PrivateStoryRepository storyRepository,
            MemberService memberService) {
        this.sceneRepository = sceneRepository;
        this.choiceRepository = choiceRepository;
        this.logRepository = logRepository;
        this.storyRepository = storyRepository;
        this.memberService = memberService;
    }

    private void saveSceneLog(String memberId, PrivateSceneResponse response, String choiceText) {
        memberService.findById(memberId)
                .map(Member::getId)
                .map(userId -> PrivateLog.builder()
                        .privateSceneId(response.getSceneId())
                        .memberId(userId)
                        .choiceText(choiceText)
                        .build())
                .ifPresent(logRepository::save);
    }

    public List<PrivateStory> getStories(String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        return storyRepository.findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
    }

    public PrivateStory createStory(String title, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        PrivateStory story = PrivateStory.builder()
                .memberId(userId)
                .title(title)
                .build();
        return storyRepository.save(story);
    }

    public void deleteStory(Long storyId, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        storyRepository.findByIdAndMemberIdAndDeletedAtIsNull(storyId, userId)
                .ifPresent(story -> {
                    story.setDeletedAt(LocalDateTime.now());
                    storyRepository.save(story);
                    List<PrivateScene> scenes = sceneRepository
                            .findAllByMemberIdAndStoryIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, storyId);
                    scenes.forEach(scene -> {
                        choiceRepository
                                .findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(userId, storyId,
                                        scene.getPrivateSceneId())
                                .forEach(c -> c.setDeletedAt(LocalDateTime.now()));
                        scene.setDeletedAt(LocalDateTime.now());
                    });
                });
    }

    public PrivateSceneResponse getFirstScene(Long storyId, String memberId) {
        return getFirstScene(storyId, memberId, memberId);
    }

    public PrivateSceneResponse getFirstScene(Long storyId, String targetMemberId, String logMemberId) {
        Long userId = memberService.findById(targetMemberId).map(Member::getId).orElse(null);
        PrivateSceneResponse response = Optional.ofNullable(userId)
                .flatMap(id -> sceneRepository.findFirstByMemberIdAndStoryIdAndIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc(id, storyId))
                .map(PrivateSceneResponse::fromEntity)
                .orElse(PrivateSceneResponse.builder()
                        .sceneId("")
                        .speaker("")
                        .backgroundImage("loading-background1.png")
                        .text("첫 번째 화면이 없습니다.")
                        .isStart(false)
                        .isEnd(false)
                        .build());
        response.setTexts(getChoiceResponsesBySceneId(response.getSceneId(), storyId, userId));
        if (logMemberId != null) saveSceneLog(logMemberId, response, null);
        return response;
    }

    public PrivateSceneResponse getPrivateScene(String id, Long storyId, String memberId, String choiceText) {
        return getPrivateScene(id, storyId, memberId, memberId, choiceText);
    }

    public PrivateSceneResponse getPrivateScene(String id, Long storyId, String targetMemberId, String logMemberId, String choiceText) {
        Long userId = memberService.findById(targetMemberId).map(Member::getId).orElseThrow();
        PrivateSceneResponse response = sceneRepository.findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(userId, storyId, id)
                .map(PrivateSceneResponse::fromEntity)
                .orElseThrow(() -> new NotFoundSceneException("not found scene id: " + id));
        response.setTexts(getChoiceResponsesBySceneId(response.getSceneId(), storyId, userId));
        saveSceneLog(logMemberId, response, choiceText);
        return response;
    }

    private List<PrivateChoiceResponse> getChoiceResponsesBySceneId(String sceneId, Long storyId, Long memberId) {
        if (memberId == null) return List.of();
        return choiceRepository.findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(memberId, storyId, sceneId).stream()
                .map(pc -> PrivateChoiceResponse.builder()
                        .text(pc.getText())
                        .nextPrivateSceneId(pc.getNextPrivateSceneId())
                        .build())
                .toList();
    }

    public void saveAll(List<PrivateSceneRequest> requests, Long storyId, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        List<String> sceneIds = requests.stream().map(PrivateSceneRequest::getSceneId).toList();
        choiceRepository.findByMemberIdAndStoryIdAndPrivateSceneIdInAndDeletedAtIsNull(userId, storyId, sceneIds)
                .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));
        List<PrivateScene> existing = sceneRepository.findByMemberIdAndStoryIdAndPrivateSceneIdInAndDeletedAtIsNull(userId, storyId, sceneIds);
        Map<String, PrivateScene> existingMap = existing.stream()
                .collect(Collectors.toMap(PrivateScene::getPrivateSceneId, Function.identity()));
        List<PrivateScene> scenesToSave = requests.stream().map(req -> {
            PrivateScene scene = existingMap.getOrDefault(req.getSceneId(),
                    PrivateScene.builder().privateSceneId(req.getSceneId()).memberId(userId).storyId(storyId).build());
            scene.setSpeaker(req.getSpeaker());
            scene.setBackgroundImage(req.getBackgroundImage());
            scene.setText(req.getText());
            scene.setIsStart(req.isStart());
            scene.setIsEnd(req.isEnd());
            return scene;
        }).toList();
        sceneRepository.saveAll(scenesToSave);
        List<PrivateChoice> choices = requests.stream()
                .flatMap(r -> r.getChoiceRequests().stream().map(cr -> cr.toEntity(r.getSceneId(), storyId, userId)))
                .toList();
        choiceRepository.saveAll(choices);
    }

    public void createOrUpdate(PrivateSceneRequest request, String id, Long storyId, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        Optional<PrivateScene> opt = sceneRepository.findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(userId, storyId, id);
        if (opt.isPresent()) {
            PrivateScene entity = opt.get();
            entity.setSpeaker(request.getSpeaker());
            entity.setBackgroundImage(request.getBackgroundImage());
            entity.setText(request.getText());
            entity.setIsStart(request.isStart());
            entity.setIsEnd(request.isEnd());
        } else {
            PrivateScene entity = request.toEntity(userId);
            entity.setPrivateSceneId(id);
            sceneRepository.save(entity);
        }
        choiceRepository.findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(userId, storyId, id)
                .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));
        List<PrivateChoice> choices = request.getChoiceRequests().stream()
                .map(cr -> {
                    PrivateChoice entity = cr.toEntity(request.getSceneId(), storyId, userId);
                    entity.setPrivateSceneId(id);
                    return entity;
                }).toList();
        choiceRepository.saveAll(choices);
    }

    public void delete(String id, Long storyId, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        sceneRepository.findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(userId, storyId, id)
                .ifPresent(scene -> {
                    choiceRepository.findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(userId, storyId, scene.getPrivateSceneId())
                            .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));
                    sceneRepository.save(scene);
                    scene.setDeletedAt(LocalDateTime.now());
                });
    }

    @Transactional(readOnly = true)
    public PrivateStoryResponse getPrivateScenes(Long storyId, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        PrivateStoryResponse resp = PrivateStoryResponse.fromEntity(sceneRepository.findAllByMemberIdAndStoryIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, storyId));
        resp.getPrivateScenes().forEach(scene -> {
            List<PrivateChoice> choices = choiceRepository.findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(userId, storyId, scene.getSceneId());
            List<PrivateChoiceResponse> list = choices.stream()
                    .map(c -> PrivateChoiceResponse.builder().text(c.getText()).nextPrivateSceneId(c.getNextPrivateSceneId()).build())
                    .toList();
            scene.setTexts(list);
        });
        return resp;
    }

    @Transactional(readOnly = true)
    public List<AdminStoryInfo> getAdminStories() {
        List<Member> admins = memberService.findByRole("ADMIN");
        if (admins.isEmpty()) return List.of();
        Map<Long, String> idMap = admins.stream()
                .collect(Collectors.toMap(Member::getId, Member::getMemberId));
        List<PrivateStory> stories = storyRepository.findByMemberIdInAndDeletedAtIsNullOrderByCreatedAtDesc(admins.stream().map(Member::getId).toList());
        return stories.stream()
                .map(story -> new AdminStoryInfo(story.getId(), story.getTitle(), idMap.get(story.getMemberId())))
                .toList();
    }
}
