package com.self_true.service;

import com.self_true.exception.DuplicateSceneIdException;
import com.self_true.exception.NotFoundSceneException;
import com.self_true.model.dto.request.PublicSceneRequest;
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
import java.util.Optional;

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
    private void saveSceneLog(String memberId, PublicSceneResponse response, Long id) {
        memberService.findById(memberId)
                .map(Member::getId)
                .map(userId -> PublicLog.builder()
                        .publicSceneId(response.getSceneId())
                        .scenePageId(id)
                        .userId(userId)
                        .build())
                .ifPresent(publicLogRepository::save);
    }

    public PublicSceneResponse getFirstScene(String memberId) {
        Optional<PublicScene> publicScene = publicSceneRepository.findFirstByIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc();
        PublicSceneResponse response = publicScene
                .map(PublicSceneResponse::fromEntity)
                .orElse(PublicSceneResponse.builder()
                        .sceneId("")
                        .speaker("")
                        .backgroundImage("loading-background1.png")
                        .text("첫 번째 화면이 없습니다.")
                        .isStart(false)
                        .isEnd(false)
                        .build());

        saveSceneLog(
                memberId,
                response,
                publicScene
                        .map(PublicScene::getId)
                        .orElse(0L)
        );

        return response;
    }

    public PublicSceneResponse getPublicScene(Long id, String memberId) {

        PublicSceneResponse response = publicSceneRepository.findByIdAndDeletedAtIsNull(id)
                .map(PublicSceneResponse::fromEntity)
                .orElseThrow(() -> new NotFoundSceneException("not found scene id: " + id));

        saveSceneLog(
                memberId,
                response,
                id
        );

        return response;
    }

    public void save(PublicSceneRequest request) {
        PublicScene publicScene = request.toEntity();
        publicSceneRepository.findByPublicSceneId(publicScene.getPublicSceneId())
                        .ifPresent(ps -> {
                            throw new DuplicateSceneIdException("Scene Id is already exists: " + publicScene.getPublicSceneId() + "[" + ps.getId() + "]");
                        });
        publicSceneRepository.save(publicScene);

        List<PublicChoice> choices = request.getChoiceRequests().stream().map(cr -> cr.toEntity(request.getSceneId())).toList();
        publicChoiceRepository.saveAll(choices);
    }

    public void saveAll(List<PublicSceneRequest> requests) {
        List<PublicScene> entities = requests.stream().map(PublicSceneRequest::toEntity).toList();
        publicSceneRepository.saveAll(entities);

        List<PublicChoice> choices = requests.stream().flatMap(rs -> rs.getChoiceRequests().stream().map(cr -> cr.toEntity(rs.getSceneId()))).toList();
        publicChoiceRepository.saveAll(choices);
    }

    public void update(PublicSceneRequest request, Long id) {
        PublicScene entity = publicSceneRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundSceneException("not found scene id: " + id));

        // 장면 수정
        entity.setPublicSceneId(request.getSceneId());
        entity.setSpeaker(request.getSpeaker());
        entity.setBackgroundImage(request.getBackgroundImage());
        entity.setText(request.getText());
        entity.setIsStart(request.isStart());
        entity.setIsEnd(request.isEnd());

        // 기존 선택지 삭제
        publicChoiceRepository.findByPublicSceneIdAndDeletedAtIsNull(request.getSceneId())
                        .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));


        // 새로운 선택지 저장
        List<PublicChoice> choices = request.getChoiceRequests().stream().map(cr -> cr.toEntity(request.getSceneId())).toList();
        publicChoiceRepository.saveAll(choices);
    }

    public void delete(Long id) {
        publicSceneRepository.findByIdAndDeletedAtIsNull(id)
                .ifPresent(scene -> {
                    publicChoiceRepository.findByPublicSceneIdAndDeletedAtIsNull(scene.getPublicSceneId())
                            .forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));
                    publicSceneRepository.save(scene);
                });
    }

    @Transactional(readOnly = true)
    public PublicStoryResponse getPublicScenes() {
        return PublicStoryResponse.fromEntity(publicSceneRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc());
    }
}
