package com.self_true.repository;

import com.self_true.model.entity.PrivateStory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrivateStoryRepository extends JpaRepository<PrivateStory, Long> {
    List<PrivateStory> findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId);
    Optional<PrivateStory> findByIdAndMemberIdAndDeletedAtIsNull(Long id, Long memberId);
}
