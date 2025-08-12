package com.phuonglinh.service;

import fa.training.lms.dto.BookSearchCriteria;
import fa.training.lms.dto.Page;
import fa.training.lms.dto.PageRequest;
import fa.training.lms.entity.Book;

import java.util.List;

public interface BookService {
    Book create(Book book);
    Book getById(Long id);
    Book update(Long id, Book book);
    void delete(Long id);

    Page<Book> search(BookSearchCriteria criteria, PageRequest pageRequest);
    List<Book> listByAuthor(Long authorId, int limit);
    List<Book> topBorrowed(int limit);

    void changeAvailability(Long id, boolean available);
    List<Book> findOverdueByDays(int days);
    Book getCached(Long id);

    // Batch operations
    void bulkImport(List<Book> books, int batchSize);
    void bulkUpdateAvailability(List<Long> bookIds, boolean available, int batchSize);
}