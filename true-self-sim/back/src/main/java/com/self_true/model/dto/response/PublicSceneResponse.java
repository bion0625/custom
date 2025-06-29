package com.self_true.model.dto.response;

import com.self_true.model.entity.PublicScene;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PublicSceneResponse {
    /**
     * 로그 저장을 위한 장면 ID
     * */
    private String sceneId;
    private String speaker;
    private String backgroundImage;
    /**
     * 질문
     * */
    private String text;
    private boolean isStart;
    private boolean isEnd;
    /**
     * 답변
     * */
    private List<PublicChoiceResponce> texts;

    public static PublicSceneResponse fromEntity(PublicScene publicScene) {
        return PublicSceneResponse.builder()
                .sceneId(publicScene.getPublicSceneId())
                .speaker(publicScene.getSpeaker())
                .backgroundImage(publicScene.getBackgroundImage())
                .text(publicScene.getText())
                .isStart(publicScene.getIsStart())
                .isEnd(publicScene.getIsEnd())
                .build();
    }
}
