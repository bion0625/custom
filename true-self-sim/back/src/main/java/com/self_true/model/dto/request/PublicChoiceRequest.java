package com.self_true.model.dto.request;

import lombok.Data;

@Data
public class PublicChoiceRequest {
    /**
     * 다음 장면
     * */
    private Long nextSceneId;
    private String text;
}
