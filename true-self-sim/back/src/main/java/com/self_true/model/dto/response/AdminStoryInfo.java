package com.self_true.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminStoryInfo {
    private Long id;
    private String title;
    private String memberId;
}
