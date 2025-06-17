package com.self_true.model.dto.request;

import com.self_true.model.entity.PublicChoice;
import com.self_true.model.entity.PublicScene;
import lombok.Data;

import java.util.List;

@Data
public class PublicSceneRequest {
    private String speaker;
    private String backgroundImage;
    /**
     * 질문
     * */
    private String text;
    private boolean isStart = false;
    private boolean isEnd = false;
    /**
     * 답변
     * */
    private List<PublicChoiceRequest> choiceRequests;

    public PublicScene toEntity() {
        return PublicScene.builder()
                .speaker(speaker)
                .backgroundImage(backgroundImage)
                .text(text)
                .isStart(isStart)
                .isEnd(isEnd)
                .publicChoices(
                        choiceRequests.stream()
                                .map(cr -> PublicChoice.builder()
                                        .nextPublicScene(PublicScene.builder().id(cr.getNextSceneId()).build())
                                        .text(cr.getText())
                                        .build())
                                .toList())
                .build();
    }
}
