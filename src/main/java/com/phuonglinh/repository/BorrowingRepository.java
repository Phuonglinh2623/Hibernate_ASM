package com.phuonglinh.repository;

import fa.training.lms.dto.Page;
import fa.training.lms.dto.PageRequest;
import fa.training.lms.entity.Borrowing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowingRepository {
    Borrowing save(Borrowing borrowing);
    Optional<Borrowing> findById(Long id);
    List<Borrowing> findAll();
    void delete(Borrowing borrowing);
    boolean existsById(Long id);

    Page<Borrowing> findByMember(Long memberId, PageRequest pageRequest);
    List<Borrowing> findActiveByBook(Long bookId);
    List<Borrowing> findOverdue(LocalDate referenceDate);
    List<Borrowing> findOverdueByDaysSp(int days);

    void saveAll(List<Borrowing> borrowings, int batchSize);
}
