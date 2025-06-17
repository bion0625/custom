package com.self_true.repository;

import com.self_true.model.entity.PublicLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicLogRepository extends JpaRepository<PublicLog, Long> {
}
