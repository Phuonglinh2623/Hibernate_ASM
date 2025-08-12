package com.phuonglinh.repository.impl;

import com.phuonglinh.dto.Page;
import com.phuonglinh.dto.PageRequest;
import com.phuonglinh.entity.Borrowing;
import com.phuonglinh.repository.BorrowingRepository;
import com.phuonglinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BorrowingRepositoryImpl implements BorrowingRepository {

    private static final Logger logger = LoggerFactory.getLogger(BorrowingRepositoryImpl.class);

    @Override
    public Borrowing save(Borrowing borrowing) {
        Session session = getCurrentSession();
        if (borrowing.getId() == null) {
            session.save(borrowing);
            logger.debug("Saved new borrowing with ID: {}", borrowing.getId());
        } else {
            session.update(borrowing);
            logger.debug("Updated borrowing with ID: {}", borrowing.getId());
        }
        return borrowing;
    }

    @Override
    public Optional<Borrowing> findById(Long id) {
        return Optional.ofNullable(getCurrentSession().get(Borrowing.class, id));
    }

    @Override
    public List<Borrowing> findAll() {
        return getCurrentSession()
                .createQuery("FROM Borrowing", Borrowing.class)
                .list();
    }

    @Override
    public void delete(Borrowing borrowing) {
        getCurrentSession().delete(borrowing);
        logger.debug("Deleted borrowing with ID: {}", borrowing.getId());
    }

    @Override
    public boolean existsById(Long id) {
        Long count = getCurrentSession()
                .createQuery("SELECT COUNT(b) FROM Borrowing b WHERE b.id = :id", Long.class)
                .setParameter("id", id)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public Page<Borrowing> findByMember(Long memberId, PageRequest pageRequest) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Borrowing> countRoot = countQuery.from(Borrowing.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.equal(countRoot.get("member").get("id"), memberId));
        Long total = session.createQuery(countQuery).uniqueResult();

        // Data query
        CriteriaQuery<Borrowing> dataQuery = cb.createQuery(Borrowing.class);
        Root<Borrowing> dataRoot = dataQuery.from(Borrowing.class);
        dataQuery.select(dataRoot);
        dataQuery.where(cb.equal(dataRoot.get("member").get("id"), memberId));
        applySorting(cb, dataQuery, dataRoot, pageRequest);

        Query<Borrowing> query = session.createQuery(dataQuery)
                .setFirstResult(pageRequest.getPage() * pageRequest.getSize())
                .setMaxResults(pageRequest.getSize());

        return new Page<>(query.list(), total != null ? total : 0, pageRequest.getPage(), pageRequest.getSize());
    }

    @Override
    public List<Borrowing> findActiveByBook(Long bookId) {
        return getCurrentSession()
                .getNamedQuery("Borrowing.findActiveByBook")
                .setParameter("bookId", bookId)
                .list();
    }

    @Override
    public List<Borrowing> findOverdue(LocalDate referenceDate) {
        return getCurrentSession()
                .createNativeQuery(
                        "SELECT * FROM borrowings WHERE status = 'BORROWED' AND due_date < ?",
                        Borrowing.class
                )
                .setParameter(1, referenceDate)
                .list();
    }

    @Override
    public List<Borrowing> findOverdueByDaysSp(int days) {
        List<Borrowing> borrowings = new ArrayList<>();
        try {
            Connection connection = getCurrentSession().doReturningWork(conn -> conn);
            try (CallableStatement stmt = connection.prepareCall("{CALL GetOverdueBorrowings(?)}")) {
                stmt.setInt(1, days);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Borrowing borrowing = new Borrowing();
                        borrowing.setId(rs.getLong("id"));
                        borrowing.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                        borrowing.setDueDate(rs.getDate("due_date").toLocalDate());
                        // Có thể set thêm các field khác nếu cần
                        borrowings.add(borrowing);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error executing stored procedure GetOverdueBorrowings", e);
        }
        return borrowings;
    }

    @Override
    public void saveAll(List<Borrowing> borrowings, int batchSize) {
        Session session = getCurrentSession();
        for (int i = 0; i < borrowings.size(); i++) {
            session.save(borrowings.get(i));
            if (i % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        session.flush();
        session.clear();
    }

    private Session getCurrentSession() {
        return HibernateUtil.getSessionFactory().getCurrentSession();
    }

    private void applySorting(CriteriaBuilder cb, CriteriaQuery<Borrowing> query, Root<Borrowing> root, PageRequest pageRequest) {
        if (pageRequest.getSorts() != null && !pageRequest.getSorts().isEmpty()) {
            List<Order> orders = new ArrayList<>();
            for (PageRequest.Sort sort : pageRequest.getSorts()) {
                orders.add(sort.getDirection() == PageRequest.SortDirection.ASC
                        ? cb.asc(root.get(sort.getField()))
                        : cb.desc(root.get(sort.getField())));
            }
            query.orderBy(orders);
        }
    }
}
