package com.self_true.repository;

import com.self_true.model.entity.PrivateScene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrivateSceneRepository extends JpaRepository<PrivateScene, Long> {
    List<PrivateScene> findAllByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId);

    Optional<PrivateScene> findFirstByMemberIdAndIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId);

    Optional<PrivateScene> findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(Long memberId, String privateSceneId);

    List<PrivateScene> findByMemberIdAndPrivateSceneIdInAndDeletedAtIsNull(Long memberId, List<String> privateSceneIds);
}
