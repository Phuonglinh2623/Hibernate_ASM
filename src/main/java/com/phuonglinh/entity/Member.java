package com.phuonglinh.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members",
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Member name is required")
    @Column(nullable = false, length = 255)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+84|0)[3-9][0-9]{8,9}$", 
             message = "Phone number must be valid Vietnamese format")
    @Column(nullable = false, length = 20)
    private String phone;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Version
    private Long version;
    
    // One-to-Many with Borrowing
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private Set<Borrowing> borrowings = new HashSet<>();
    
    // Constructors
    public Member() {}
    
    public Member(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
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
    
    public Set<Borrowing> getBorrowings() {
        return borrowings;
    }
    
    public void setBorrowings(Set<Borrowing> borrowings) {
        this.borrowings = borrowings;
    }
    
    // Utility methods
    public void addBorrowing(Borrowing borrowing) {
        this.borrowings.add(borrowing);
        borrowing.setMember(this);
    }
    
    public void removeBorrowing(Borrowing borrowing) {
        this.borrowings.remove(borrowing);
        borrowing.setMember(null);
    }
    
    // Business logic helper methods
    public long getActiveBorrowingsCount() {
        return borrowings.stream()
                .filter(borrowing -> borrowing.getStatus() == BorrowingStatus.BORROWED)
                .count();
    }
    
    public boolean isEligibleToBorrow() {
        return getActiveBorrowingsCount() < 5;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return getId() != null && getId().equals(member.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", createdDate=" + createdDate +
                ", version=" + version +
                '}';
    }
}