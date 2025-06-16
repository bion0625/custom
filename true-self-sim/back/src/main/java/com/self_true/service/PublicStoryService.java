package com.self_true.service;

import com.self_true.exception.NotFoundSceneException;
import com.self_true.model.dto.request.PublicChoiceRequest;
import com.self_true.model.dto.request.PublicSceneRequest;
import com.self_true.model.dto.response.PublicSceneResponse;
import com.self_true.model.dto.response.PublicStoryResponse;
import com.self_true.model.entity.PublicChoice;
import com.self_true.model.entity.PublicScene;
import com.self_true.repository.PublicChoiceRepository;
import com.self_true.repository.PublicSceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PublicStoryService {
    private final PublicSceneRepository publicSceneRepository;
    private final PublicChoiceRepository publicChoiceRepository;

    public PublicStoryService(PublicSceneRepository publicSceneRepository, PublicChoiceRepository publicChoiceRepository) {
        this.publicSceneRepository = publicSceneRepository;
        this.publicChoiceRepository = publicChoiceRepository;
    }

    //todo 추후 관리자에서 편집할 때 is_start 플래그가 하나 있으면 나머지 사용할 수 없으니 단 번에 가져와서 구분하고 단번에 저장해야 함
    @Transactional(readOnly = true)
    public PublicStoryResponse getPublicScenes() {
        return PublicStoryResponse.fromEntity(publicSceneRepository.findAllByDeletedAtIsNull());
    }

    @Transactional(readOnly = true)
    public PublicSceneResponse getPublicScene(PublicChoiceRequest request) {
        return Optional.ofNullable(request)
                .map(PublicChoiceRequest::getNextSceneId)
                .map(publicSceneRepository::findById)
                .orElseGet(publicSceneRepository::findFirstByIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc)
                .map(PublicSceneResponse::fromEntity)
                .orElseThrow(() ->
                        Optional.ofNullable(request)
                                .map(PublicChoiceRequest::getNextSceneId)
                                .map(id -> new NotFoundSceneException("not fount next scene id: " + id))
                                .orElse(new NotFoundSceneException("not fount next scene id is null")));
    }

    public void save(PublicSceneRequest request) {
        PublicScene publicScene = request.toEntity();
        publicSceneRepository.save(publicScene);
        for (PublicChoice publicChoice : publicScene.getPublicChoices()) {
            publicChoice.setPublicScene(publicScene);
        }
        publicChoiceRepository.saveAll(publicScene.getPublicChoices());
    }
}
