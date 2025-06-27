package com.self_true.service;

import com.self_true.exception.NotFoundSceneException;
import com.self_true.model.dto.request.PrivateSceneRequest;
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
    private final MemberService memberService;

    public PrivateStoryService(
            PrivateSceneRepository sceneRepository,
            PrivateChoiceRepository choiceRepository,
            PrivateLogRepository logRepository,
            MemberService memberService) {
        this.sceneRepository = sceneRepository;
        this.choiceRepository = choiceRepository;
        this.logRepository = logRepository;
        this.memberService = memberService;
    }

    private void saveSceneLog(String memberId, PrivateSceneResponse response) {
        memberService.findById(memberId)
                .map(Member::getId)
                .map(userId -> PrivateLog.builder()
                        .privateSceneId(response.getSceneId())
                        .memberId(userId)
                        .build())
                .ifPresent(logRepository::save);
    }

    public PrivateSceneResponse getFirstScene(String memberId) {
        return getFirstScene(memberId, memberId);
    }

    public PrivateSceneResponse getFirstScene(String targetMemberId, String logMemberId) {
        Long userId = memberService.findById(targetMemberId).map(Member::getId).orElse(null);
        PrivateSceneResponse response = Optional.ofNullable(userId)
                .flatMap(id -> sceneRepository.findFirstByMemberIdAndIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc(id))
                .map(PrivateSceneResponse::fromEntity)
                .orElse(PrivateSceneResponse.builder()
                        .sceneId("")
                        .speaker("")
                        .backgroundImage("loading-background1.png")
                        .text("첫 번째 화면이 없습니다.")
                        .isStart(false)
                        .isEnd(false)
                        .build());
        response.setTexts(getChoiceResponsesBySceneId(response.getSceneId(), userId));
        if (logMemberId != null) saveSceneLog(logMemberId, response);
        return response;
    }

    public PrivateSceneResponse getPrivateScene(String id, String memberId) {
        return getPrivateScene(id, memberId, memberId);
    }

    public PrivateSceneResponse getPrivateScene(String id, String targetMemberId, String logMemberId) {
        Long userId = memberService.findById(targetMemberId).map(Member::getId).orElseThrow();
        PrivateSceneResponse response = sceneRepository.findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(userId, id)
                .map(PrivateSceneResponse::fromEntity)
                .orElseThrow(() -> new NotFoundSceneException("not found scene id: " + id));
        response.setTexts(getChoiceResponsesBySceneId(response.getSceneId(), userId));
        saveSceneLog(logMemberId, response);
        return response;
    }

    private List<PrivateChoiceResponse> getChoiceResponsesBySceneId(String sceneId, Long memberId) {
        if (memberId == null) return List.of();
        return choiceRepository.findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(memberId, sceneId).stream()
                .map(pc -> PrivateChoiceResponse.builder()
                        .text(pc.getText())
                        .nextPrivateSceneId(pc.getNextPrivateSceneId())
                        .build())
                .toList();
    }

    public void saveAll(List<PrivateSceneRequest> requests, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        List<String> sceneIds = requests.stream().map(PrivateSceneRequest::getSceneId).toList();
        choiceRepository.findByMemberIdAndPrivateSceneIdInAndDeletedAtIsNull(userId, sceneIds)
                .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));
        List<PrivateScene> existing = sceneRepository.findByMemberIdAndPrivateSceneIdInAndDeletedAtIsNull(userId, sceneIds);
        Map<String, PrivateScene> existingMap = existing.stream()
                .collect(Collectors.toMap(PrivateScene::getPrivateSceneId, Function.identity()));
        List<PrivateScene> scenesToSave = requests.stream().map(req -> {
            PrivateScene scene = existingMap.getOrDefault(req.getSceneId(),
                    PrivateScene.builder().privateSceneId(req.getSceneId()).memberId(userId).build());
            scene.setSpeaker(req.getSpeaker());
            scene.setBackgroundImage(req.getBackgroundImage());
            scene.setText(req.getText());
            scene.setIsStart(req.isStart());
            scene.setIsEnd(req.isEnd());
            return scene;
        }).toList();
        sceneRepository.saveAll(scenesToSave);
        List<PrivateChoice> choices = requests.stream()
                .flatMap(r -> r.getChoiceRequests().stream().map(cr -> cr.toEntity(r.getSceneId(), userId)))
                .toList();
        choiceRepository.saveAll(choices);
    }

    public void createOrUpdate(PrivateSceneRequest request, String id, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        Optional<PrivateScene> opt = sceneRepository.findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(userId, id);
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
        choiceRepository.findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(userId, id)
                .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));
        List<PrivateChoice> choices = request.getChoiceRequests().stream()
                .map(cr -> {
                    PrivateChoice entity = cr.toEntity(request.getSceneId(), userId);
                    entity.setPrivateSceneId(id);
                    return entity;
                }).toList();
        choiceRepository.saveAll(choices);
    }

    public void delete(String id, String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        sceneRepository.findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(userId, id)
                .ifPresent(scene -> {
                    choiceRepository.findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(userId, scene.getPrivateSceneId())
                            .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));
                    sceneRepository.save(scene);
                    scene.setDeletedAt(LocalDateTime.now());
                });
    }

    @Transactional(readOnly = true)
    public PrivateStoryResponse getPrivateScenes(String memberId) {
        Long userId = memberService.findById(memberId).map(Member::getId).orElseThrow();
        PrivateStoryResponse resp = PrivateStoryResponse.fromEntity(sceneRepository.findAllByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId));
        resp.getPrivateScenes().forEach(scene -> {
            List<PrivateChoice> choices = choiceRepository.findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(userId, scene.getSceneId());
            List<PrivateChoiceResponse> list = choices.stream()
                    .map(c -> PrivateChoiceResponse.builder().text(c.getText()).nextPrivateSceneId(c.getNextPrivateSceneId()).build())
                    .toList();
            scene.setTexts(list);
        });
        return resp;
    }
}
