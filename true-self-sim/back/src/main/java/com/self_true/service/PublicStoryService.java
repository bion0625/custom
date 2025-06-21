package com.self_true.service;

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
import java.util.ArrayList;
import java.util.List;

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
                .map(id -> PublicLog.builder()
                        .publicSceneId(response.getSceneId())
                        .userId(id)
                        .build())
                .ifPresent(publicLogRepository::save);
    }

    public PublicSceneResponse getFirstScene(String memberId) {
        PublicSceneResponse response = publicSceneRepository.findFirstByIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc()
                .map(PublicSceneResponse::fromEntity)
                .orElse(PublicSceneResponse.builder()
                        .sceneId(0L)
                        .speaker("")
                        .backgroundImage("loading-background1.png")
                        .text("첫 번째 화면이 없습니다.")
                        .isStart(false)
                        .isEnd(false)
                        .build());

        saveSceneLog(memberId, response);

        return response;
    }

    public PublicSceneResponse getPublicScene(Long id, String memberId) {

        PublicSceneResponse response = publicSceneRepository.findByIdAndDeletedAtIsNull(id)
                .map(PublicSceneResponse::fromEntity)
                .orElseThrow(() -> new NotFoundSceneException("not found scene id: " + id));

        saveSceneLog(memberId, response);

        return response;
    }

    public void saveAll(List<PublicSceneRequest> requests) {
        List<PublicScene> entities = requests.stream().map(PublicSceneRequest::toEntity).toList();
        publicSceneRepository.saveAll(entities);

        List<PublicChoice> allChoices = new ArrayList<>();

        entities.forEach(e ->
            e.getPublicChoices().forEach(pc -> {
                pc.setPublicScene(e);
                allChoices.add(pc);
            })
        );

        publicChoiceRepository.saveAll(allChoices);
    }

    public void save(PublicSceneRequest request) {
        PublicScene publicScene = request.toEntity();
        publicSceneRepository.save(publicScene);
        for (PublicChoice publicChoice : publicScene.getPublicChoices()) {
            publicChoice.setPublicScene(publicScene);
        }
        publicChoiceRepository.saveAll(publicScene.getPublicChoices());
    }

    public void update(PublicSceneRequest request, Long id) {
        PublicScene entity = publicSceneRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundSceneException("not found scene id: " + id));

        entity.getPublicChoices()
                .forEach(publicChoice -> publicChoice.setDeletedAt(LocalDateTime.now()));

        PublicScene update = request.toEntity();

        entity.setSpeaker(update.getSpeaker());
        entity.setBackgroundImage(update.getBackgroundImage());
        entity.setText(update.getText());
        entity.setIsStart(update.getIsStart());
        entity.setIsEnd(update.getIsEnd());
        entity.setPublicChoices(update.getPublicChoices());
        entity.getPublicChoices().forEach(cr -> cr.setDeletedAt(LocalDateTime.now()));

        publicChoiceRepository.saveAll(request.getChoiceRequests().stream()
                .map(cr -> PublicChoice.builder()
                        .text(cr.getText())
                        .publicScene(PublicScene.builder().id(id).build())
                        .nextPublicScene(PublicScene.builder().id(cr.getNextSceneId()).build())
                        .build())
                .toList());
    }

    public void delete(Long id) {
        publicSceneRepository.findByIdAndDeletedAtIsNull(id)
                .ifPresent(scene -> {
                    scene.getPublicChoices()
                            .forEach(publicChoice -> publicChoice.setDeletedAt(LocalDateTime.now()));
                    publicChoiceRepository.saveAll(scene.getPublicChoices());
                    scene.setDeletedAt(LocalDateTime.now());
                    publicSceneRepository.save(scene);
                });
    }

    @Transactional(readOnly = true)
    public PublicStoryResponse getPublicScenes() {
        return PublicStoryResponse.fromEntity(publicSceneRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc());
    }
}
