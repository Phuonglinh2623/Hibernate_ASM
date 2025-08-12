package com.phuonglinh.service;

import fa.training.lms.dto.BorrowBooksRequest;
import fa.training.lms.dto.Page;
import fa.training.lms.dto.PageRequest;
import fa.training.lms.entity.Borrowing;

import java.time.LocalDate;
import java.util.List;

public interface BorrowingService {
    List<Borrowing> borrowBooks(BorrowBooksRequest request);
    void returnBooks(List<Long> borrowingIds, LocalDate returnDate);
    void extendDueDate(Long borrowingId, LocalDate newDueDate);

    Page<Borrowing> findByMember(Long memberId, PageRequest pageRequest);
    List<Borrowing> findActiveByBook(Long bookId);
    List<Borrowing> findOverdue(LocalDate referenceDate);
    List<Borrowing> findOverdueByDaysSp(int days);
}