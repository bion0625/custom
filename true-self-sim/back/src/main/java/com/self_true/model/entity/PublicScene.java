package com.self_true.model.entity;

import com.self_true.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
public class PublicScene extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String speaker;
    private String backgroundImage;
    private String text;
    private Boolean isStart;
    private Boolean isEnd;

    @OneToMany(mappedBy = "publicScene")
    private List<PublicChoice> publicChoices;
}
