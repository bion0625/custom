package com.self_true.service;

import com.self_true.exception.NotFoundMemberException;
import com.self_true.exception.NotFoundSceneException;
import com.self_true.model.dto.request.PublicChoiceRequest;
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

    public PublicSceneResponse getPublicScene(PublicChoiceRequest request, String memberId) {
        PublicSceneResponse response = Optional.ofNullable(request)
                .map(PublicChoiceRequest::getNextSceneId)
                .map(publicSceneRepository::findByIdAndDeletedAtIsNull)
                .orElseGet(publicSceneRepository::findFirstByIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc)
                .map(PublicSceneResponse::fromEntity)
                .orElseThrow(() ->
                        Optional.ofNullable(request)
                                .map(PublicChoiceRequest::getNextSceneId)
                                .map(id -> new NotFoundSceneException("not fount next scene id: " + id))
                                .orElse(new NotFoundSceneException("not fount next scene id is null")));

        Long userId = memberService.findById(memberId)
                .map(Member::getId)
                .orElseThrow(() -> new NotFoundMemberException("not found member id: " + memberId));

        publicLogRepository.save(PublicLog
                .builder()
                .publicSceneId(response.getSceneId())
                .userId(userId)
                .build());

        return response;
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

        entity.getPublicChoices()
                .forEach(publicChoice -> publicChoice.setPublicScene(PublicScene.builder().id(id).build()));
        publicChoiceRepository.saveAll(entity.getPublicChoices());
    }

    @Transactional(readOnly = true)
    public PublicStoryResponse getPublicScenes() {
        return PublicStoryResponse.fromEntity(publicSceneRepository.findAllByDeletedAtIsNull());
    }
}
