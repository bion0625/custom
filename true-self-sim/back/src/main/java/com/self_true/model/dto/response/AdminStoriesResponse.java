package com.self_true.model.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class AdminStoriesResponse extends Response {
    private List<AdminStoryInfo> stories;

    public AdminStoriesResponse(List<AdminStoryInfo> stories) {
        this.stories = stories;
        setIsSuccess(true);
    }
}
