package com.phuonglinh.entity;


import com.phuonglinh.enums.BorrowingStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "borrowings")
@NamedQueries({
        @NamedQuery(
                name = "Borrowing.findOverdue",
                query = "SELECT b FROM Borrowing b WHERE b.status = 'BORROWED' AND b.dueDate < :referenceDate"
        ),
        @NamedQuery(
                name = "Borrowing.findActiveByMember",
                query = "SELECT b FROM Borrowing b WHERE b.member.id = :memberId AND b.status = 'BORROWED'"
        ),
        @NamedQuery(
                name = "Borrowing.findActiveByBook",
                query = "SELECT b FROM Borrowing b WHERE b.book.id = :bookId AND b.status = 'BORROWED'"
        )
})
public class Borrowing extends BaseEntity {

    @NotNull(message = "Member is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull(message = "Book is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull(message = "Borrow date is required")
    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BorrowingStatus status = BorrowingStatus.BORROWED;

    // Constructors
    public Borrowing() {}

    public Borrowing(Member member, Book book, LocalDate borrowDate, LocalDate dueDate) {
        this.member = member;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Borrowing{" +
                "id=" + getId() +
                ", member=" + (member != null ? member.getName() : "null") +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                '}';
    }
}
