package com.phuonglinh.repository.impl;

import fa.training.lms.dto.BookSearchCriteria;
import fa.training.lms.dto.Page;
import fa.training.lms.dto.PageRequest;
import fa.training.lms.entity.Book;
import fa.training.lms.enums.BorrowingStatus;
import fa.training.lms.repository.BookRepository;
import fa.training.lms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookRepositoryImpl implements BookRepository {

    private static final Logger logger = LoggerFactory.getLogger(BookRepositoryImpl.class);

    @Override
    public Book save(Book book) {
        Session session = getSession();
        if (book.getId() == null) {
            session.save(book);
            logger.debug("Book saved with ID: {}", book.getId());
        } else {
            session.update(book);
            logger.debug("Book updated with ID: {}", book.getId());
        }
        return book;
    }

    @Override
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(getSession().get(Book.class, id));
    }

    @Override
    public List<Book> findAll() {
        return getSession().createQuery("FROM Book", Book.class).list();
    }

    @Override
    public void delete(Book book) {
        getSession().delete(book);
        logger.debug("Book deleted with ID: {}", book.getId());
    }

    @Override
    public boolean existsById(Long id) {
        Long count = getSession().createQuery("SELECT COUNT(b) FROM Book b WHERE b.id = :id", Long.class)
                .setParameter("id", id)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public Page<Book> search(BookSearchCriteria criteria, PageRequest pageRequest) {
        Session session = getSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Book> countRoot = countQuery.from(Book.class);
        countQuery.select(cb.count(countRoot));
        Predicate[] countPredicates = buildPredicates(cb, countRoot, criteria);
        if (countPredicates.length > 0) countQuery.where(countPredicates);
        long totalElements = Optional.ofNullable(session.createQuery(countQuery).uniqueResult()).orElse(0L);

        CriteriaQuery<Book> dataQuery = cb.createQuery(Book.class);
        Root<Book> dataRoot = dataQuery.from(Book.class);
        dataQuery.select(dataRoot);
        Predicate[] dataPredicates = buildPredicates(cb, dataRoot, criteria);
        if (dataPredicates.length > 0) dataQuery.where(dataPredicates);

        if (pageRequest.getSorts() != null && !pageRequest.getSorts().isEmpty()) {
            List<Order> orders = new ArrayList<>();
            for (PageRequest.Sort sort : pageRequest.getSorts()) {
                orders.add(sort.getDirection() == PageRequest.SortDirection.ASC
                        ? cb.asc(dataRoot.get(sort.getField()))
                        : cb.desc(dataRoot.get(sort.getField())));
            }
            dataQuery.orderBy(orders);
        }

        Query<Book> query = session.createQuery(dataQuery);
        query.setFirstResult(pageRequest.getPage() * pageRequest.getSize());
        query.setMaxResults(pageRequest.getSize());

        return new Page<>(query.list(), totalElements, pageRequest.getPage(), pageRequest.getSize());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<Book> root, BookSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();
        if (criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + criteria.getTitle().toLowerCase() + "%"));
        }
        if (criteria.getCategory() != null && !criteria.getCategory().trim().isEmpty()) {
            predicates.add(cb.equal(root.get("category"), criteria.getCategory()));
        }
        if (criteria.getAvailable() != null) {
            predicates.add(cb.equal(root.get("available"), criteria.getAvailable()));
        }
        if (criteria.getAuthorName() != null && !criteria.getAuthorName().trim().isEmpty()) {
            Join<Object, Object> authorJoin = root.join("authors", JoinType.INNER);
            predicates.add(cb.like(cb.lower(authorJoin.get("name")), "%" + criteria.getAuthorName().toLowerCase() + "%"));
        }
        if (criteria.getCreatedFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedFrom()));
        }
        if (criteria.getCreatedTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedTo()));
        }
        return predicates.toArray(new Predicate[0]);
    }

    @Override
    public List<Book> findByAuthor(Long authorId, int limit) {
        return getSession().getNamedQuery("Book.findByAuthor")
                .setParameter("authorId", authorId)
                .setMaxResults(limit)
                .setCacheable(true)
                .list();
    }

    @Override
    public List<Book> findTopBorrowed(int limit) {
        return getSession().getNamedQuery("Book.findTopBorrowed")
                .setMaxResults(limit)
                .setCacheable(true)
                .list();
    }

    @Override
    public boolean hasActiveBorrowings(Long bookId) {
        Long count = getSession().createQuery(
                        "SELECT COUNT(b) FROM Borrowing b WHERE b.book.id = :bookId AND b.status = :status", Long.class)
                .setParameter("bookId", bookId)
                .setParameter("status", BorrowingStatus.BORROWED)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public void changeAvailability(Long id, boolean available) {
        getSession().createQuery("UPDATE Book SET available = :available WHERE id = :id")
                .setParameter("available", available)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public List<Book> findOverdueByDays(int days) {
        List<Book> books = new ArrayList<>();
        try {
            Connection connection = getSession().doReturningWork(conn -> conn);
            CallableStatement stmt = connection.prepareCall("{CALL GetOverdueBooks(?)}");
            stmt.setInt(1, days);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getLong("id"));
                book.setTitle(rs.getString("title"));
                book.setCategory(rs.getString("category"));
                book.setAvailable(rs.getBoolean("available"));
                books.add(book);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            logger.error("Error calling stored procedure", e);
        }
        return books;
    }

    @Override
    public void saveAll(List<Book> books, int batchSize) {
        Session session = getSession();
        for (int i = 0; i < books.size(); i++) {
            session.save(books.get(i));
            if (i % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        session.flush();
        session.clear();
    }

    @Override
    public void updateAvailabilityBatch(List<Long> bookIds, boolean available, int batchSize) {
        Session session = getSession();
        for (int i = 0; i < bookIds.size(); i += batchSize) {
            List<Long> batch = bookIds.subList(i, Math.min(i + batchSize, bookIds.size()));
            session.createQuery("UPDATE Book SET available = :available WHERE id IN :ids")
                    .setParameter("available", available)
                    .setParameterList("ids", batch)
                    .executeUpdate();
            session.flush();
        }
    }

    private Session getSession() {
        return HibernateUtil.getSessionFactory().getCurrentSession();
    }
}
