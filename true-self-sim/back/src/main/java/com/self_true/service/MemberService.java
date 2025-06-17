package com.self_true.service;

import com.self_true.exception.DuplicateMemberIdException;
import com.self_true.model.dto.request.MembersRequest;
import com.self_true.model.entity.Member;
import com.self_true.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public MemberService(PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    public void register(MembersRequest request) {
        if (Boolean.TRUE.equals(memberRepository.existsByMemberIdAndDeletedAtIsNull(request.getId()))) {
            throw new DuplicateMemberIdException("member Id is already exists: " + request.getId());
        }
        Member entity = request.toEntity(passwordEncoder);
        memberRepository.save(entity);
    }

    public Optional<Member> findById(String memberId) {
        return memberRepository.findByMemberIdAndDeletedAtIsNull(memberId);
    }
}
