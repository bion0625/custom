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
    private void saveSceneLog(String memberId, PublicSceneResponse response) {
        memberService.findById(memberId)
                .map(Member::getId)
                .map(userId -> PublicLog.builder()
                        .publicSceneId(response.getSceneId())
                        .userId(userId)
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

        saveSceneLog(memberId, response);

        return response;
    }

    public PublicSceneResponse getPublicScene(String id, String memberId) {

        PublicSceneResponse response = publicSceneRepository.findByPublicSceneIdAndDeletedAtIsNull(id)
                .map(PublicSceneResponse::fromEntity)
                .orElseThrow(() -> new NotFoundSceneException("not found scene id: " + id));

        saveSceneLog(memberId, response);

        return response;
    }

    public void saveAll(List<PublicSceneRequest> requests) {
        List<PublicScene> entities = requests.stream().map(PublicSceneRequest::toEntity).toList();
        publicSceneRepository.saveAll(entities);

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
