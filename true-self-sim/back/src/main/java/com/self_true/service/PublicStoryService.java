package com.self_true.service;

import com.self_true.model.dto.PublicSceneRequest;
import com.self_true.model.dto.PublicStoryResponse;
import com.self_true.repository.PublicSceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PublicStoryService {
    private final PublicSceneRepository publicSceneRepository;

    public PublicStoryService(PublicSceneRepository publicSceneRepository) {
        this.publicSceneRepository = publicSceneRepository;
    }

    @Transactional(readOnly = true)
    public PublicStoryResponse getPublicScenes() {
        return PublicStoryResponse.fromEntity(publicSceneRepository.findAll());
    }

    public void save(PublicSceneRequest request) {
        //TODO
    }
}
