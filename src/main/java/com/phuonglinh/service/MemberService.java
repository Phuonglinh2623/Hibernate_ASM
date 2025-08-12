package com.phuonglinh.service;

import com.phuonglinh.dto.Page;
import com.phuonglinh.dto.PageRequest;
import com.phuonglinh.entity.Member;

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
