package com.self_true.model.entity;

import com.self_true.model.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PublicScene extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String speaker;
    private String backgroundImage;
    private String text;
    private String isEnd;
    private String isStart;
}
