package com.self_true.model.dto;

import com.self_true.model.entity.PublicScene;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PublicStoryResponse extends Response {
    List<PublicSceneResponse> publicScenes;

    public PublicStoryResponse(List<PublicSceneResponse> publicScenes) {
        this.publicScenes = publicScenes;
        setIsSuccess(true);
    }

    public static PublicStoryResponse fromEntity(List<PublicScene> publicScenes) {
        return new PublicStoryResponse(publicScenes.stream().map(PublicSceneResponse::fromEntity).toList());
    }
}
