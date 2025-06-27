package com.self_true.model.dto.request;

import com.self_true.model.entity.PrivateChoice;
import lombok.Data;

@Data
public class PrivateChoiceRequest {
    private String nextSceneId;
    private String text;

    private Long storyId;

    public PrivateChoice toEntity(String privateSceneId, Long storyId, Long memberId) {
        return PrivateChoice.builder()
                .text(this.text)
                .nextPrivateSceneId(this.nextSceneId)
                .privateSceneId(privateSceneId)
                .storyId(storyId)
                .memberId(memberId)
                .build();
    }
}
