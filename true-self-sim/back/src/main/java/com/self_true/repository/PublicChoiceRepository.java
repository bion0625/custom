package com.self_true.repository;

import com.self_true.model.entity.PublicChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicChoiceRepository extends JpaRepository<PublicChoice, Long> {
    List<PublicChoice> findByPublicSceneIdAndDeletedAtIsNull(String publicSceneId);
    List<PublicChoice> findByPublicSceneIdInAndDeletedAtIsNull(List<String> publicSceneIds);
}
