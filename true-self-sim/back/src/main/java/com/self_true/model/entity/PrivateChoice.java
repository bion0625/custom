package com.self_true.model.entity;

import com.self_true.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChoice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private String privateSceneId;
    private String nextPrivateSceneId;

    private Long memberId;
}

