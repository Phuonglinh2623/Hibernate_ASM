package com.phuonglinh.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 255)
    private String title;
    
    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 100)
    private String category;
    
    @NotNull(message = "Available status is required")
    @Column(nullable = false)
    private Boolean available = true;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Version
    private Long version;
    
    // Many-to-Many with Author
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();
    
    // One-to-Many with Borrowing
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private Set<Borrowing> borrowings = new HashSet<>();
    
    // Constructors
    public Book() {}
    
    public Book(String title, String category) {
        this.title = title;
        this.category = category;
        this.available = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Boolean getAvailable() {
        return available;
    }
    
    public void setAvailable(Boolean available) {
        this.available = available;
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
    
    public Set<Author> getAuthors() {
        return authors;
    }
    
    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }
    
    public Set<Borrowing> getBorrowings() {
        return borrowings;
    }
    
    public void setBorrowings(Set<Borrowing> borrowings) {
        this.borrowings = borrowings;
    }
    
    // Utility methods
    public void addAuthor(Author author) {
        this.authors.add(author);
        author.getBooks().add(this);
    }
    
    public void removeAuthor(Author author) {
        this.authors.remove(author);
        author.getBooks().remove(this);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return getId() != null && getId().equals(book.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", available=" + available +
                ", createdDate=" + createdDate +
                ", version=" + version +
                '}';
    }
}