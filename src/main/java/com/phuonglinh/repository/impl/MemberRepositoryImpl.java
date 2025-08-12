package com.phuonglinh.repository.impl;

import com.phuonglinh.dto.Page;
import com.phuonglinh.dto.PageRequest;
import com.phuonglinh.entity.Member;
import com.phuonglinh.enums.BorrowingStatus;
import com.phuonglinh.repository.MemberRepository;
import com.phuonglinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberRepositoryImpl implements MemberRepository {

    private static final Logger logger = LoggerFactory.getLogger(MemberRepositoryImpl.class);

    @Override
    public Member save(Member member) {
        Session session = getCurrentSession();
        if (member.getId() == null) {
            session.save(member);
            logger.debug("Saved new member with ID: {}", member.getId());
        } else {
            session.update(member);
            logger.debug("Updated member with ID: {}", member.getId());
        }
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(getCurrentSession().get(Member.class, id));
    }

    @Override
    public List<Member> findAll() {
        return getCurrentSession()
                .createQuery("FROM Member", Member.class)
                .list();
    }

    @Override
    public void delete(Member member) {
        getCurrentSession().delete(member);
        logger.debug("Deleted member with ID: {}", member.getId());
    }

    @Override
    public boolean existsById(Long id) {
        Long count = getCurrentSession()
                .createQuery("SELECT COUNT(m) FROM Member m WHERE m.id = :id", Long.class)
                .setParameter("id", id)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return Optional.ofNullable(
                getCurrentSession()
                        .createQuery("FROM Member WHERE email = :email", Member.class)
                        .setParameter("email", email)
                        .uniqueResult()
        );
    }

    @Override
    public Page<Member> search(String searchTerm, PageRequest pageRequest) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        Predicate searchPredicate = buildSearchPredicate(cb, cb.createQuery(Long.class).from(Member.class), searchTerm);

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Member> countRoot = countQuery.from(Member.class);
        countQuery.select(cb.count(countRoot));
        if (searchPredicate != null) {
            countQuery.where(buildSearchPredicate(cb, countRoot, searchTerm));
        }
        Long total = session.createQuery(countQuery).uniqueResult();

        // Data query
        CriteriaQuery<Member> dataQuery = cb.createQuery(Member.class);
        Root<Member> dataRoot = dataQuery.from(Member.class);
        dataQuery.select(dataRoot);
        if (searchPredicate != null) {
            dataQuery.where(buildSearchPredicate(cb, dataRoot, searchTerm));
        }
        applySorting(cb, dataQuery, dataRoot, pageRequest);

        Query<Member> query = session.createQuery(dataQuery)
                .setFirstResult(pageRequest.getPage() * pageRequest.getSize())
                .setMaxResults(pageRequest.getSize());

        return new Page<>(query.list(), total != null ? total : 0, pageRequest.getPage(), pageRequest.getSize());
    }

    @Override
    public long countActiveBorrowings(Long memberId) {
        Long count = getCurrentSession()
                .createQuery("SELECT COUNT(b) FROM Borrowing b WHERE b.member.id = :memberId AND b.status = :status", Long.class)
                .setParameter("memberId", memberId)
                .setParameter("status", BorrowingStatus.BORROWED)
                .uniqueResult();
        return count != null ? count : 0;
    }

    @Override
    public boolean hasActiveBorrowings(Long memberId) {
        return countActiveBorrowings(memberId) > 0;
    }

    private Session getCurrentSession() {
        return HibernateUtil.getSessionFactory().getCurrentSession();
    }

    private Predicate buildSearchPredicate(CriteriaBuilder cb, Root<Member> root, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return null;
        }
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        Predicate name = cb.like(cb.lower(root.get("name")), pattern);
        Predicate email = cb.like(cb.lower(root.get("email")), pattern);
        Predicate phone = cb.like(root.get("phone"), pattern);
        return cb.or(name, email, phone);
    }

    private void applySorting(CriteriaBuilder cb, CriteriaQuery<Member> query, Root<Member> root, PageRequest pageRequest) {
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
