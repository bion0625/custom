package com.self_true.repository;

import com.self_true.model.entity.PrivateScene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrivateSceneRepository extends JpaRepository<PrivateScene, Long> {
    List<PrivateScene> findAllByMemberIdAndStoryIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId, Long storyId);

    Optional<PrivateScene> findFirstByMemberIdAndStoryIdAndIsStartIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId, Long storyId);

    Optional<PrivateScene> findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(Long memberId, Long storyId, String privateSceneId);

    List<PrivateScene> findByMemberIdAndStoryIdAndPrivateSceneIdInAndDeletedAtIsNull(Long memberId, Long storyId, List<String> privateSceneIds);
}
