package com.self_true.model.dto;

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
    private boolean isStart;
    private boolean isEnd;
    /**
     * 답변
     * */
    private List<String> texts;
}
