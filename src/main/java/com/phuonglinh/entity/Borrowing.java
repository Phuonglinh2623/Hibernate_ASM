package com.phuonglinh.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowings")
public class Borrowing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Borrow date is required")
    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;
    
    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date must be in the future or present")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Column(name = "return_date")
    private LocalDate returnDate;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status = BorrowingStatus.BORROWED;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Version
    private Long version;
    
    // Many-to-One with Member
    @NotNull(message = "Member is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    // Many-to-One with Book
    @NotNull(message = "Book is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    // Constructors
    public Borrowing() {}
    
    public Borrowing(LocalDate borrowDate, LocalDate dueDate, Member member, Book book) {
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.member = member;
        this.book = book;
        this.status = BorrowingStatus.BORROWED;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
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
    
    // Business logic helper methods
    public boolean isOverdue() {
        if (status == BorrowingStatus.RETURNED) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }
    
    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }
    
    public void returnBook() {
        this.status = BorrowingStatus.RETURNED;
        this.returnDate = LocalDate.now();
    }
    
    public boolean canExtendDueDate(LocalDate newDueDate) {
        return status == BorrowingStatus.BORROWED && 
               newDueDate != null && 
               newDueDate.isAfter(this.dueDate);
    }
    
    public void extendDueDate(LocalDate newDueDate) {
        if (!canExtendDueDate(newDueDate)) {
            throw new IllegalArgumentException("Cannot extend due date: invalid new date or book already returned");
        }
        this.dueDate = newDueDate;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Borrowing)) return false;
        Borrowing borrowing = (Borrowing) o;
        return getId() != null && getId().equals(borrowing.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Borrowing{" +
                "id=" + id +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                ", createdDate=" + createdDate +
                ", version=" + version +
                '}';
    }
}