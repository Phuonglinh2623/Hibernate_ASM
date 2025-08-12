package com.phuonglinh.repository;

import com.phuonglinh.dto.BookSearchCriteria;
import com.phuonglinh.dto.Page;
import com.phuonglinh.dto.PageRequest;
import com.phuonglinh.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(Long id);
    List<Book> findAll();
    void delete(Book book);
    boolean existsById(Long id);

    Page<Book> search(BookSearchCriteria criteria, PageRequest pageRequest);
    List<Book> findByAuthor(Long authorId, int limit);
    List<Book> findTopBorrowed(int limit);

    boolean hasActiveBorrowings(Long bookId);
    void changeAvailability(Long id, boolean available);

    List<Book> findOverdueByDays(int days);

    void saveAll(List<Book> books, int batchSize);
    void updateAvailabilityBatch(List<Long> bookIds, boolean available, int batchSize);
}
