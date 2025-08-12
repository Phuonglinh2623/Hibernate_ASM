package com.phuonglinh.repository;

import com.phuonglinh.dto.Page;
import com.phuonglinh.dto.PageRequest;
import com.phuonglinh.entity.Member;

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
