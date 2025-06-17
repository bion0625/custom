package com.self_true.repository;

import com.self_true.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Boolean existsByMemberIdAndDeletedAtIsNull(String memberId);
    Optional<Member> findByMemberIdAndDeletedAtIsNull(String memberId);
}
