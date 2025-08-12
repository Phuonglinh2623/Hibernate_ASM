package com.phuonglinh.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class BorrowBooksRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotEmpty(message = "At least one book ID is required")
    private List<Long> bookIds;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    // Constructors
    public BorrowBooksRequest() {}

    public BorrowBooksRequest(Long memberId, List<Long> bookIds, LocalDate dueDate) {
        this.memberId = memberId;
        this.bookIds = bookIds;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public List<Long> getBookIds() {
        return bookIds;
    }

    public void setBookIds(List<Long> bookIds) {
        this.bookIds = bookIds;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
