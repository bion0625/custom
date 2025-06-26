package com.self_true.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivateChoiceResponse {
    private String text;
    private String nextPrivateSceneId;
    private String nextText;
}
