package com.self_true.service;

import com.self_true.model.dto.PublicSceneResponse;
import com.self_true.model.dto.PublicStoryResponse;
import com.self_true.model.entity.PublicScene;
import com.self_true.repository.PublicSceneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicStoryService {
    private final PublicSceneRepository publicSceneRepository;

    public PublicStoryService(PublicSceneRepository publicSceneRepository) {
        this.publicSceneRepository = publicSceneRepository;
    }

    public PublicStoryResponse getPublicScenes() {
        return PublicStoryResponse.fromEntity(publicSceneRepository.findAll());
    }
}
