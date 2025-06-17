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
public class PublicChoice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;

    @ManyToOne
    @JoinColumn(name = "public_scene_id")
    private PublicScene publicScene;

    @ManyToOne
    @JoinColumn(name = "next_public_scene_id")
    private PublicScene nextPublicScene;
}
