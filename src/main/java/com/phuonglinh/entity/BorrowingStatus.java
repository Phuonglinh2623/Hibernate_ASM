package com.phuonglinh.entity;

public enum BorrowingStatus {
    BORROWED("Borrowed"),
    RETURNED("Returned");
    
    private final String displayName;
    
    BorrowingStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
