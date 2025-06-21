package com.self_true.model.entity;

import com.self_true.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class PublicScene extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String publicSceneId;
    private String speaker;
    private String backgroundImage;
    private String text;
    private Boolean isStart;
    private Boolean isEnd;
}
