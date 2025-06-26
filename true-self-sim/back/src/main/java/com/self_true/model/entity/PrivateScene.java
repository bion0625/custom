package com.self_true.model.entity;

import com.self_true.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class PrivateScene extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String privateSceneId;

    private String speaker;
    private String backgroundImage;
    private String text;
    private Boolean isStart;
    private Boolean isEnd;

    private Long memberId;
}

