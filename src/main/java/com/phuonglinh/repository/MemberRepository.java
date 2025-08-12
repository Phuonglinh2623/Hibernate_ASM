package com.phuonglinh.repository;

import fa.training.lms.dto.Page;
import fa.training.lms.dto.PageRequest;
import fa.training.lms.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    List<Member> findAll();
    void delete(Member member);
    boolean existsById(Long id);

    Optional<Member> findByEmail(String email);
    Page<Member> search(String searchTerm, PageRequest pageRequest);

    long countActiveBorrowings(Long memberId);
    boolean hasActiveBorrowings(Long memberId);
}
