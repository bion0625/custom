package com.self_true.service;

import com.self_true.exception.NotFoundSceneException;
import com.self_true.model.dto.request.PublicSceneRequest;
import com.self_true.model.dto.response.PublicChoiceResponce;
import com.self_true.model.dto.response.PublicSceneResponse;
import com.self_true.model.dto.response.PublicStoryResponse;
import com.self_true.model.entity.Member;
import com.self_true.model.entity.PublicChoice;
import com.self_true.model.entity.PublicLog;
import com.self_true.model.entity.PublicScene;
import com.self_true.repository.PublicChoiceRepository;
import com.self_true.repository.PublicLogRepository;
import com.self_true.repository.PublicSceneRepository;
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
public class PublicStoryService {
    private final PublicSceneRepository publicSceneRepository;
    private final PublicChoiceRepository publicChoiceRepository;
    private final PublicLogRepository publicLogRepository;

    private final MemberService memberService;

    public PublicStoryService(
            PublicSceneRepository publicSceneRepository,
            PublicChoiceRepository publicChoiceRepository,
            PublicLogRepository publicLogRepository,
            MemberService memberService) {
        this.publicSceneRepository = publicSceneRepository;
        this.publicChoiceRepository = publicChoiceRepository;
        this.publicLogRepository = publicLogRepository;
        this.memberService = memberService;
    }

    /**
     * 로그인 한 상태일 때만 저장
     * */
    private void saveSceneLog(String memberId, PublicSceneResponse response, String choiceText) {
        memberService.findById(memberId)
                .map(Member::getId)
                .map(userId -> PublicLog.builder()
                        .publicSceneId(response.getSceneId())
                        .userId(userId)
                        .choiceText(choiceText)
                        .build())
                .ifPresent(publicLogRepository::save);
    }

    public PublicSceneResponse getFirstScene(String memberId) {
        PublicSceneResponse response = publicSceneRepository.findFirstByIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc()
                .map(PublicSceneResponse::fromEntity)
                .orElse(PublicSceneResponse.builder()
                        .sceneId("")
                        .speaker("")
                        .backgroundImage("loading-background1.png")
                        .text("첫 번째 화면이 없습니다.")
                        .isStart(false)
                        .isEnd(false)
                        .build());
        response.setTexts(getChoiceResponcesBySceneId(response.getSceneId()));

        saveSceneLog(memberId, response, null);

        return response;
    }

    public PublicSceneResponse getPublicScene(String id, String memberId, String choiceText) {

        PublicSceneResponse response = publicSceneRepository.findByPublicSceneIdAndDeletedAtIsNull(id)
                .map(PublicSceneResponse::fromEntity)
                .orElseThrow(() -> new NotFoundSceneException("not found scene id: " + id));
        response.setTexts(getChoiceResponcesBySceneId(response.getSceneId()));

        saveSceneLog(memberId, response, choiceText);

        return response;
    }

    private List<PublicChoiceResponce> getChoiceResponcesBySceneId(String sceneId) {
        return publicChoiceRepository.findByPublicSceneIdAndDeletedAtIsNull(sceneId).stream()
                .map(pc -> PublicChoiceResponce.builder()
                        .text(pc.getText())
                        .nextPublicSceneId(pc.getNextPublicSceneId())
                        .build())
                .toList();
    }

    public void saveAll(List<PublicSceneRequest> requests) {

        // 1) 요청에 포함된 sceneId 리스트 뽑기
        List<String> sceneIds = requests.stream()
                .map(PublicSceneRequest::getSceneId)
                .toList();

        // 1-1) 존재하는 관련 선택지들 삭제
        publicChoiceRepository.findByPublicSceneIdInAndDeletedAtIsNull(sceneIds)
                .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));

        // 2) DB에서 기존에 저장된 엔티티 한 번에 조회
        List<PublicScene> existingScenes = publicSceneRepository.findByPublicSceneIdInAndDeletedAtIsNull(sceneIds);
        Map<String, PublicScene> existingMap = existingScenes.stream()
                .collect(Collectors.toMap(PublicScene::getPublicSceneId, Function.identity()));

        // 3) 요청별로 업서트용 엔티티 준비
        List<PublicScene> scenesToSave = requests.stream()
                .map(req -> {
                    PublicScene scene = existingMap.getOrDefault(req.getSceneId(),
                            // 없으면 새로 빌더로 생성
                            PublicScene.builder()
                                    .publicSceneId(req.getSceneId())
                                    .build()
                    );
                    // 공통 필드 업데이트
                    scene.setSpeaker(req.getSpeaker());
                    scene.setBackgroundImage(req.getBackgroundImage());
                    scene.setText(req.getText());
                    scene.setIsStart(req.isStart());
                    scene.setIsEnd(req.isEnd());
                    return scene;
                })
                .toList();

        // 4) 저장 (JPA가 id가 null인 건 INSERT, 있으면 UPDATE 처리)
        publicSceneRepository.saveAll(scenesToSave);

        List<PublicChoice> choices = requests.stream().flatMap(rs -> rs.getChoiceRequests().stream().map(cr -> cr.toEntity(rs.getSceneId()))).toList();
        publicChoiceRepository.saveAll(choices);
    }

    public void createOrUpdate(PublicSceneRequest request, String id) {
        Optional<PublicScene> optEntity = publicSceneRepository.findByPublicSceneIdAndDeletedAtIsNull(id);

        if (optEntity.isPresent()) {
            PublicScene entity = optEntity.get();
            // 장면 수정
            entity.setSpeaker(request.getSpeaker());
            entity.setBackgroundImage(request.getBackgroundImage());
            entity.setText(request.getText());
            entity.setIsStart(request.isStart());
            entity.setIsEnd(request.isEnd());
        } else {
            PublicScene entity = request.toEntity();
            entity.setPublicSceneId(id);
            publicSceneRepository.save(entity);
        }

        // 기존 선택지 삭제
        publicChoiceRepository.findByPublicSceneIdAndDeletedAtIsNull(id)
                .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));


        // 새로운 선택지 저장
        List<PublicChoice> choices = request.getChoiceRequests().stream()
                .map(cr -> {
                    PublicChoice entity = cr.toEntity(request.getSceneId());
                    entity.setPublicSceneId(id);
                    return entity;
                }).toList();
        publicChoiceRepository.saveAll(choices);
    }

    public void delete(String id) {
        publicSceneRepository.findByPublicSceneIdAndDeletedAtIsNull(id)
                .ifPresent(scene -> {
                    publicChoiceRepository.findByPublicSceneIdAndDeletedAtIsNull(scene.getPublicSceneId())
                            .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));
                    publicSceneRepository.save(scene);
                    scene.setDeletedAt(LocalDateTime.now());
                });
    }

    @Transactional(readOnly = true)
    public PublicStoryResponse getPublicScenes() {
        PublicStoryResponse publicStoryResponse = PublicStoryResponse.fromEntity(publicSceneRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc());
        publicStoryResponse.getPublicScenes().forEach(scene -> {
            List<PublicChoice> choices = publicChoiceRepository.findByPublicSceneIdAndDeletedAtIsNull(scene.getSceneId());
            List<PublicChoiceResponce> list = choices.stream().map(c -> PublicChoiceResponce.builder().text(c.getText()).nextPublicSceneId(c.getNextPublicSceneId()).build()).toList();
            scene.setTexts(list);
        });
        return publicStoryResponse;
    }
}
