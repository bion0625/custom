package com.self_true.model.dto.response;

import com.self_true.model.entity.PrivateScene;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PrivateStoryResponse extends Response {
    List<PrivateSceneResponse> privateScenes;

    public PrivateStoryResponse(List<PrivateSceneResponse> privateScenes) {
        this.privateScenes = privateScenes;
        setIsSuccess(true);
    }

    public static PrivateStoryResponse fromEntity(List<PrivateScene> scenes) {
        return new PrivateStoryResponse(scenes.stream().map(PrivateSceneResponse::fromEntity).toList());
    }
}
