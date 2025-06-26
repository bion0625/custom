package com.self_true.model.dto.request;

import com.self_true.model.entity.PrivateChoice;
import com.self_true.model.entity.PrivateScene;
import lombok.Data;

import java.util.List;

@Data
public class PrivateSceneRequest {
    private String sceneId;
    private String speaker;
    private String backgroundImage;
    private String text;
    private boolean isStart = false;
    private boolean isEnd = false;
    private List<PrivateChoiceRequest> choiceRequests;

    public PrivateScene toEntity(Long memberId) {
        return PrivateScene.builder()
                .privateSceneId(sceneId)
                .speaker(speaker)
                .backgroundImage(backgroundImage)
                .text(text)
                .isStart(isStart)
                .isEnd(isEnd)
                .memberId(memberId)
                .build();
    }

    public List<PrivateChoice> toChoiceEntities(Long memberId) {
        return choiceRequests.stream()
                .map(cr -> cr.toEntity(sceneId, memberId))
                .toList();
    }
}
