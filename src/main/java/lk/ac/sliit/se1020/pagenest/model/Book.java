package lk.ac.sliit.se1020.pagenest.model;

import java.math.BigDecimal;

public class Book {

    // Other classes use these details through methods instead of changing the fields directly.
    private long id;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private BigDecimal price;
    private int stock;
    private String description;
    private String coverUrl;

    public Book() {
    }

    public Book(long id, String isbn, String title, String author, String category,
                BigDecimal price, int stock, String description, String coverUrl) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.coverUrl = coverUrl;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    // Existing plain Book objects are treated as printed books for compatibility.
    public String getFormat() { return "Printed Book"; }
}
