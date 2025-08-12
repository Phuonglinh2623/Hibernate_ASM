package com.phuonglinh.validation;

import com.phuonglinh.entity.Member;
import com.phuonglinh.util.HibernateUtil;
import org.hibernate.Session;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, Member> {

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(Member member, ConstraintValidatorContext context) {
        if (member == null || member.getEmail() == null) {
            return true;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(m) FROM Member m WHERE m.email = :email AND m.id != :id",
                            Long.class)
                    .setParameter("email", member.getEmail())
                    .setParameter("id", member.getId() != null ? member.getId() : -1L)
                    .uniqueResult();

            return count == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
