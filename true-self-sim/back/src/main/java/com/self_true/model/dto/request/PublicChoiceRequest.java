package com.self_true.model.dto.request;

import com.self_true.model.entity.PublicChoice;
import lombok.Data;

@Data
public class PublicChoiceRequest {
    /**
     * 다음 장면
     * */
    private String nextSceneId;
    private String text;

    public PublicChoice toEntity(String publicSceneId) {
        return PublicChoice.builder()
                .text(this.text)
                .nextPublicSceneId(this.nextSceneId)
                .publicSceneId(publicSceneId)
                .build();
    }
}
