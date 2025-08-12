package com.phuonglinh.service.impl;

import com.phuonglinh.dto.BorrowBooksRequest;
import com.phuonglinh.dto.Page;
import com.phuonglinh.dto.PageRequest;
import com.phuonglinh.entity.Book;
import com.phuonglinh.entity.Borrowing;
import com.phuonglinh.entity.Member;
import com.phuonglinh.enums.BorrowingStatus;
import com.phuonglinh.exception.BusinessRuleViolationException;
import com.phuonglinh.exception.EntityNotFoundException;
import com.phuonglinh.repository.BookRepository;
import com.phuonglinh.repository.BorrowingRepository;
import com.phuonglinh.repository.MemberRepository;
import com.phuonglinh.repository.impl.BookRepositoryImpl;
import com.phuonglinh.repository.impl.BorrowingRepositoryImpl;
import com.phuonglinh.repository.impl.MemberRepositoryImpl;
import com.phuonglinh.service.BorrowingService;
import com.phuonglinh.util.HibernateUtil;
import com.phuonglinh.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowingServiceImpl implements BorrowingService {
    private static final Logger log = LoggerFactory.getLogger(BorrowingServiceImpl.class);
    private final BorrowingRepository borrowingRepo;
    private final BookRepository bookRepo;
    private final MemberRepository memberRepo;

    public BorrowingServiceImpl() {
        this.borrowingRepo = new BorrowingRepositoryImpl();
        this.bookRepo = new BookRepositoryImpl();
        this.memberRepo = new MemberRepositoryImpl();
    }

    @Override
    public List<Borrowing> borrowBooks(BorrowBooksRequest request) {
        String traceId = MDC.get("correlationId");
        long start = System.nanoTime();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            ValidationUtil.validate(request);

            Member member = memberRepo.findById(request.getMemberId())
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with ID: " + request.getMemberId()));

            List<Borrowing> results = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (Long bookId : request.getBookIds()) {
                Book book = bookRepo.findById(bookId)
                        .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + bookId));

                if (!book.getAvailable()) {
                    throw new BusinessRuleViolationException("Book not available: " + book.getTitle());
                }

                Borrowing borrowing = new Borrowing(member, book, today, request.getDueDate());
                ValidationUtil.validate(borrowing);
                results.add(borrowingRepo.save(borrowing));

                book.setAvailable(false);
                bookRepo.save(book);
            }

            tx.commit();
            log.info("[{}] Borrowed {} books in {} ms", traceId, results.size(), (System.nanoTime() - start) / 1_000_000);
            return results;
        } catch (Exception e) {
            log.error("[{}] Failed to borrow books", traceId, e);
            throw e;
        }
    }

    @Override
    public void returnBooks(List<Long> borrowingIds, LocalDate returnDate) {
        String traceId = MDC.get("correlationId");
        long start = System.nanoTime();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            for (Long id : borrowingIds) {
                Borrowing b = borrowingRepo.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with ID: " + id));

                if (b.getStatus() != BorrowingStatus.RETURNED) {
                    b.setReturnDate(returnDate);
                    b.setStatus(BorrowingStatus.RETURNED);
                    borrowingRepo.save(b);

                    Book book = b.getBook();
                    book.setAvailable(true);
                    bookRepo.save(book);
                }
            }

            tx.commit();
            log.info("[{}] Returned {} borrowings in {} ms", traceId, borrowingIds.size(), (System.nanoTime() - start) / 1_000_000);
        } catch (Exception e) {
            log.error("[{}] Failed to return books", traceId, e);
            throw e;
        }
    }

    @Override
    public void extendDueDate(Long borrowingId, LocalDate newDueDate) {
        String traceId = MDC.get("correlationId");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Borrowing borrowing = borrowingRepo.findById(borrowingId)
                    .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with ID: " + borrowingId));

            if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
                throw new BusinessRuleViolationException("Cannot extend due date for returned book");
            }
            if (!newDueDate.isAfter(borrowing.getDueDate()) || !newDueDate.isAfter(LocalDate.now())) {
                throw new BusinessRuleViolationException("New due date must be after current due date and today");
            }

            borrowing.setDueDate(newDueDate);
            borrowingRepo.save(borrowing);

            tx.commit();
            log.info("[{}] Extended due date for borrowing ID: {} to {}", traceId, borrowingId, newDueDate);
        } catch (Exception e) {
            log.error("[{}] Failed to extend due date for borrowing ID: {}", traceId, borrowingId, e);
            throw e;
        }
    }

    @Override
    public Page<Borrowing> findByMember(Long memberId, PageRequest pageRequest) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return borrowingRepo.findByMember(memberId, pageRequest);
        }
    }

    @Override
    public List<Borrowing> findActiveByBook(Long bookId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return borrowingRepo.findActiveByBook(bookId);
        }
    }

    @Override
    public List<Borrowing> findOverdue(LocalDate referenceDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return borrowingRepo.findOverdue(referenceDate);
        }
    }

    @Override
    public List<Borrowing> findOverdueByDaysSp(int days) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return borrowingRepo.findOverdueByDaysSp(days);
        }
    }
}
