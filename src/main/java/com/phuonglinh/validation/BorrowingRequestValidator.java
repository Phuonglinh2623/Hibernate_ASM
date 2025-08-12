package com.phuonglinh.validation;

import com.phuonglinh.dto.BorrowBooksRequest;
import com.phuonglinh.entity.Borrowing;
import com.phuonglinh.enums.BorrowingStatus;
import com.phuonglinh.util.HibernateUtil;
import org.hibernate.Session;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.util.List;

public class BorrowingRequestValidator implements ConstraintValidator<ValidBorrowingRequest, Object> {

    @Override
    public void initialize(ValidBorrowingRequest constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof BorrowBooksRequest) {
            return validateBorrowBooksRequest((BorrowBooksRequest) obj, context);
        } else if (obj instanceof Borrowing) {
            return validateBorrowing((Borrowing) obj, context);
        }
        return true;
    }

    private boolean validateBorrowBooksRequest(BorrowBooksRequest request, ConstraintValidatorContext context) {
        boolean isValid = true;

        // Validate due date is after current date
        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDate.now())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Due date must be in the future")
                    .addConstraintViolation();
            isValid = false;
        }

        if (request.getMemberId() != null && request.getBookIds() != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Check member's active borrowings
                Long activeBorrowings = session.createQuery(
                                "SELECT COUNT(b) FROM Borrowing b WHERE b.member.id = :memberId AND b.status = :status",
                                Long.class)
                        .setParameter("memberId", request.getMemberId())
                        .setParameter("status", BorrowingStatus.BORROWED)
                        .uniqueResult();

                if (activeBorrowings + request.getBookIds().size() > 5) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Member cannot borrow more than 5 books")
                            .addConstraintViolation();
                    isValid = false;
                }

                // Check if all books are available
                List<Long> unavailableBooks = session.createQuery(
                                "SELECT b.id FROM Book b WHERE b.id IN :bookIds AND b.available = false",
                                Long.class)
                        .setParameterList("bookIds", request.getBookIds())
                        .list();

                if (!unavailableBooks.isEmpty()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Some books are not available: " + unavailableBooks)
                            .addConstraintViolation();
                    isValid = false;
                }
            } catch (Exception e) {
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateBorrowing(Borrowing borrowing, ConstraintValidatorContext context) {
        if (borrowing.getBorrowDate() != null && borrowing.getDueDate() != null) {
            if (borrowing.getDueDate().isBefore(borrowing.getBorrowDate())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Due date must be after borrow date")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}

