package com.self_true.model.entity;

import com.self_true.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PublicChoice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;

    @ManyToOne
    @JoinColumn(name = "publicSceneId")
    private PublicScene publicScene;
}
