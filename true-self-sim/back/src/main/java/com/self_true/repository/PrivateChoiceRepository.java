package com.self_true.repository;

import com.self_true.model.entity.PrivateChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivateChoiceRepository extends JpaRepository<PrivateChoice, Long> {
    List<PrivateChoice> findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(Long memberId, String privateSceneId);
    List<PrivateChoice> findByMemberIdAndPrivateSceneIdInAndDeletedAtIsNull(Long memberId, List<String> privateSceneIds);
}
