package com.self_true.model.dto.response;

import com.self_true.model.entity.PrivateScene;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PrivateSceneResponse {
    private String sceneId;
    private String speaker;
    private String backgroundImage;
    private String text;
    private boolean isStart;
    private boolean isEnd;
    private List<PrivateChoiceResponse> texts;

    public static PrivateSceneResponse fromEntity(PrivateScene entity) {
        return PrivateSceneResponse.builder()
                .sceneId(entity.getPrivateSceneId())
                .speaker(entity.getSpeaker())
                .backgroundImage(entity.getBackgroundImage())
                .text(entity.getText())
                .isStart(entity.getIsStart())
                .isEnd(entity.getIsEnd())
                .build();
    }
}
