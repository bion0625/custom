package com.self_true.repository;

import com.self_true.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Boolean existsByMemberId(String memberId);
}
