package com.self_true.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicChoiceResponce {
    private String text;
    private String nextPublicSceneId;
    private String nextText;
}
