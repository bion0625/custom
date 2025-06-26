package com.self_true.model.entity;

import com.self_true.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class PrivateLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private String privateSceneId;
}
