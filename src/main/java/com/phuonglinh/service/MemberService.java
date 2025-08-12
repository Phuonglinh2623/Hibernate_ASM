package com.phuonglinh.service;

import fa.training.lms.dto.Page;
import fa.training.lms.dto.PageRequest;
import fa.training.lms.entity.Member;

import java.util.Optional;

public interface MemberService {
    Member register(Member member);
    Member getById(Long id);
    Member update(Long id, Member member);
    void delete(Long id);

    Optional<Member> findByEmail(String email);
    Page<Member> search(String searchTerm, PageRequest pageRequest);

    boolean isEligibleToBorrow(Long memberId);
    long countActiveBorrowings(Long memberId);
}
