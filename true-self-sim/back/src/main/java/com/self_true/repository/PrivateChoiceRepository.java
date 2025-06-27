package com.self_true.repository;

import com.self_true.model.entity.PrivateChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivateChoiceRepository extends JpaRepository<PrivateChoice, Long> {
    List<PrivateChoice> findByMemberIdAndStoryIdAndPrivateSceneIdAndDeletedAtIsNull(Long memberId, Long storyId, String privateSceneId);
    List<PrivateChoice> findByMemberIdAndStoryIdAndPrivateSceneIdInAndDeletedAtIsNull(Long memberId, Long storyId, List<String> privateSceneIds);
}
