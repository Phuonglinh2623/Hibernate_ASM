package com.phuonglinh.service.impl;

import com.phuonglinh.dto.BookSearchCriteria;
import com.phuonglinh.dto.Page;
import com.phuonglinh.dto.PageRequest;
import com.phuonglinh.entity.Book;
import com.phuonglinh.exception.BusinessRuleViolationException;
import com.phuonglinh.exception.EntityNotFoundException;
import com.phuonglinh.repository.BookRepository;
import com.phuonglinh.repository.impl.BookRepositoryImpl;
import com.phuonglinh.service.BookService;
import com.phuonglinh.util.HibernateUtil;
import com.phuonglinh.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;

public class BookServiceImpl implements BookService {
    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);
    private final BookRepository repository;

    public BookServiceImpl() {
        this.repository = new BookRepositoryImpl();
    }

    @Override
    public Book create(Book book) {
        String traceId = MDC.get("correlationId");
        long start = System.nanoTime();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            ValidationUtil.validate(book);
            Book result = repository.save(book);
            tx.commit();
            log.info("[{}] Created book '{}' (ID: {}) in {} ms", 
                    traceId, result.getTitle(), result.getId(),
                    (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (Exception ex) {
            log.error("[{}] Failed to create book '{}'", traceId, book.getTitle(), ex);
            throw ex;
        }
    }

    @Override
    public Book getById(Long id) {
        String traceId = MDC.get("correlationId");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("No book found with ID: " + id));
        } catch (Exception ex) {
            log.error("[{}] Error fetching book ID: {}", traceId, id, ex);
            throw ex;
        }
    }

    @Override
    public Book update(Long id, Book book) {
        String traceId = MDC.get("correlationId");
        long start = System.nanoTime();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Book current = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("No book found with ID: " + id));
            current.setTitle(book.getTitle());
            current.setCategory(book.getCategory());
            current.setDescription(book.getDescription());
            current.setIsbn(book.getIsbn());
            ValidationUtil.validate(current);
            Book updated = repository.save(current);
            tx.commit();
            log.info("[{}] Updated book ID: {} in {} ms", 
                    traceId, id, (System.nanoTime() - start) / 1_000_000);
            return updated;
        } catch (Exception ex) {
            log.error("[{}] Failed to update book ID: {}", traceId, id, ex);
            throw ex;
        }
    }

    @Override
    public void delete(Long id) {
        String traceId = MDC.get("correlationId");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Book target = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("No book found with ID: " + id));
            if (repository.hasActiveBorrowings(id)) {
                throw new BusinessRuleViolationException("Book has active borrowings and cannot be deleted");
            }
            repository.delete(target);
            tx.commit();
            log.info("[{}] Deleted book ID: {}", traceId, id);
        } catch (Exception ex) {
            log.error("[{}] Failed to delete book ID: {}", traceId, id, ex);
            throw ex;
        }
    }

    @Override
    public Page<Book> search(BookSearchCriteria criteria, PageRequest pageRequest) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return repository.search(criteria, pageRequest);
        }
    }

    @Override
    public List<Book> listByAuthor(Long authorId, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return repository.findByAuthor(authorId, limit);
        }
    }

    @Override
    public List<Book> topBorrowed(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return repository.findTopBorrowed(limit);
        }
    }

    @Override
    public void changeAvailability(Long id, boolean available) {
        String traceId = MDC.get("correlationId");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            if (!repository.existsById(id)) {
                throw new EntityNotFoundException("No book found with ID: " + id);
            }
            repository.changeAvailability(id, available);
            tx.commit();
            log.info("[{}] Changed availability of book ID: {} to {}", traceId, id, available);
        } catch (Exception ex) {
            log.error("[{}] Failed to change availability for book ID: {}", traceId, id, ex);
            throw ex;
        }
    }

    @Override
    public List<Book> findOverdueByDays(int days) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return repository.findOverdueByDays(days);
        }
    }

    @Override
    public Book getCached(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Book.class, id);
        }
    }

    @Override
    public void bulkImport(List<Book> books, int batchSize) {
        String traceId = MDC.get("correlationId");
        long start = System.nanoTime();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            for (Book b : books) {
                ValidationUtil.validate(b);
            }
            repository.saveAll(books, batchSize);
            tx.commit();
            log.info("[{}] Imported {} books in {} ms", 
                    traceId, books.size(), (System.nanoTime() - start) / 1_000_000);
        } catch (Exception ex) {
            log.error("[{}] Bulk import failed", traceId, ex);
            throw ex;
        }
    }

    @Override
    public void bulkUpdateAvailability(List<Long> bookIds, boolean available, int batchSize) {
        String traceId = MDC.get("correlationId");
        long start = System.nanoTime();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            repository.updateAvailabilityBatch(bookIds, available, batchSize);
            tx.commit();
            log.info("[{}] Bulk updated {} books in {} ms", 
                    traceId, bookIds.size(), (System.nanoTime() - start) / 1_000_000);
        } catch (Exception ex) {
            log.error("[{}] Bulk update availability failed", traceId, ex);
            throw ex;
        }
    }
}
