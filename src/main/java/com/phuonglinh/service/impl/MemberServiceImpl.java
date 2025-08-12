package com.phuonglinh.service.impl;

import fa.training.lms.dto.Page;
import fa.training.lms.dto.PageRequest;
import fa.training.lms.entity.Member;
import fa.training.lms.exception.BusinessRuleViolationException;
import fa.training.lms.exception.DuplicateResourceException;
import fa.training.lms.exception.EntityNotFoundException;
import fa.training.lms.repository.MemberRepository;
import fa.training.lms.repository.impl.MemberRepositoryImpl;
import fa.training.lms.service.MemberService;
import fa.training.lms.util.HibernateUtil;
import fa.training.lms.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Optional;

public class MemberServiceImpl implements MemberService {
    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);
    private final MemberRepository memberRepo;

    public MemberServiceImpl() {
        this.memberRepo = new MemberRepositoryImpl();
    }

    @Override
    public Member register(Member member) {
        String corrId = MDC.get("correlationId");
        long start = System.currentTimeMillis();
        log.info("[{}] Start register member: {}", corrId, member.getEmail());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                ValidationUtil.validate(member);
                ensureEmailUnique(member.getEmail(), corrId);

                Member saved = memberRepo.save(member);
                tx.commit();

                log.info("[{}] Member registered: {} ({}ms)", corrId, saved.getId(),
                        System.currentTimeMillis() - start);
                return saved;
            } catch (Exception e) {
                tx.rollback();
                log.error("[{}] Register failed: {}", corrId, e.getMessage(), e);
                throw e;
            }
        }
    }

    @Override
    public Member getById(Long id) {
        String corrId = MDC.get("correlationId");
        log.debug("[{}] Get member by ID: {}", corrId, id);
        try (Session ignored = HibernateUtil.getSessionFactory().openSession()) {
            return memberRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with ID: " + id));
        }
    }

    @Override
    public Member update(Long id, Member member) {
        String corrId = MDC.get("correlationId");
        long start = System.currentTimeMillis();
        log.info("[{}] Start update member ID: {}", corrId, id);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Member existing = memberRepo.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Member not found with ID: " + id));

                if (!existing.getEmail().equals(member.getEmail())) {
                    ensureEmailUnique(member.getEmail(), corrId);
                }

                existing.setName(member.getName());
                existing.setEmail(member.getEmail());
                existing.setPhone(member.getPhone());
                existing.setAddress(member.getAddress());

                ValidationUtil.validate(existing);

                Member updated = memberRepo.save(existing);
                tx.commit();

                log.info("[{}] Member updated: {} ({}ms)", corrId, id,
                        System.currentTimeMillis() - start);
                return updated;
            } catch (Exception e) {
                tx.rollback();
                log.error("[{}] Update failed for ID {}: {}", corrId, id, e.getMessage(), e);
                throw e;
            }
        }
    }

    @Override
    public void delete(Long id) {
        String corrId = MDC.get("correlationId");
        log.info("[{}] Deleting member ID: {}", corrId, id);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Member member = memberRepo.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Member not found with ID: " + id));

                if (memberRepo.hasActiveBorrowings(id)) {
                    log.warn("[{}] Cannot delete member ID {} - active borrowings exist", corrId, id);
                    throw new BusinessRuleViolationException("Cannot delete member with active borrowings");
                }

                memberRepo.delete(member);
                tx.commit();
                log.info("[{}] Member deleted: {}", corrId, id);
            } catch (Exception e) {
                tx.rollback();
                log.error("[{}] Delete failed for ID {}: {}", corrId, id, e.getMessage(), e);
                throw e;
            }
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String corrId = MDC.get("correlationId");
        log.debug("[{}] Find member by email: {}", corrId, email);
        try (Session ignored = HibernateUtil.getSessionFactory().openSession()) {
            return memberRepo.findByEmail(email);
        }
    }

    @Override
    public Page<Member> search(String searchTerm, PageRequest pageRequest) {
        String corrId = MDC.get("correlationId");
        log.debug("[{}] Search members: {}", corrId, searchTerm);
        try (Session ignored = HibernateUtil.getSessionFactory().openSession()) {
            return memberRepo.search(searchTerm, pageRequest);
        }
    }

    @Override
    public boolean isEligibleToBorrow(Long memberId) {
        String corrId = MDC.get("correlationId");
        log.debug("[{}] Check eligibility for ID: {}", corrId, memberId);
        try (Session ignored = HibernateUtil.getSessionFactory().openSession()) {
            return memberRepo.countActiveBorrowings(memberId) <= 4;
        }
    }

    @Override
    public long countActiveBorrowings(Long memberId) {
        String corrId = MDC.get("correlationId");
        log.debug("[{}] Count active borrowings for ID: {}", corrId, memberId);
        try (Session ignored = HibernateUtil.getSessionFactory().openSession()) {
            return memberRepo.countActiveBorrowings(memberId);
        }
    }

    private void ensureEmailUnique(String email, String corrId) {
        if (memberRepo.findByEmail(email).isPresent()) {
            log.warn("[{}] Email already exists: {}", corrId, email);
            throw new DuplicateResourceException("Email already exists: " + email);
        }
    }
}
