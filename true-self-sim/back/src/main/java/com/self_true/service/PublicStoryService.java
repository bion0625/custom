package com.self_true.service;

import com.self_true.model.dto.PublicSceneRequest;
import com.self_true.model.dto.PublicStoryResponse;
import com.self_true.model.entity.PublicChoice;
import com.self_true.model.entity.PublicScene;
import com.self_true.repository.PublicChoiceRepository;
import com.self_true.repository.PublicSceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PublicStoryService {
    private final PublicSceneRepository publicSceneRepository;
    private final PublicChoiceRepository publicChoiceRepository;

    public PublicStoryService(PublicSceneRepository publicSceneRepository, PublicChoiceRepository publicChoiceRepository) {
        this.publicSceneRepository = publicSceneRepository;
        this.publicChoiceRepository = publicChoiceRepository;
    }

    @Transactional(readOnly = true)
    public PublicStoryResponse getPublicScenes() {
        return PublicStoryResponse.fromEntity(publicSceneRepository.findAllByDeletedAtIsNull());
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
