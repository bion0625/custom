package com.self_true.repository;

import com.self_true.model.entity.PublicScene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicSceneRepository extends JpaRepository<PublicScene, Long> {
    List<PublicScene> findAllByDeletedAtIsNull();
}
