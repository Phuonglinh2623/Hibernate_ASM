package com.phuonglinh.repository;

import com.phuonglinh.dto.Page;
import com.phuonglinh.dto.PageRequest;
import com.phuonglinh.entity.Borrowing;

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
