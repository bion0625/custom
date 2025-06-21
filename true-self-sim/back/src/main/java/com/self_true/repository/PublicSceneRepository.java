package com.self_true.repository;

import com.self_true.model.entity.PublicScene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicSceneRepository extends JpaRepository<PublicScene, Long> {
    List<PublicScene> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<PublicScene> findFirstByIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<PublicScene> findByPublicSceneIdAndDeletedAtIsNull(String PublicSceneId);

    Optional<PublicScene> findByPublicSceneId(String publicSceneId);
}
