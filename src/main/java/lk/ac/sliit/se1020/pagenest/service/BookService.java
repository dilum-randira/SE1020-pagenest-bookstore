package lk.ac.sliit.se1020.pagenest.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.model.EBook;
import lk.ac.sliit.se1020.pagenest.model.PrintedBook;
import lk.ac.sliit.se1020.pagenest.repository.BookFileRepository;

@Service
public class BookService {

    private final BookFileRepository repository;

    public BookService(BookFileRepository repository) {
        this.repository = repository;
        repository.initializeIfMissing(demoBooks());
    }

    // Used only when books.txt does not exist yet. All normal reads come from the file.
    private static List<Book> demoBooks() {
        return List.of(
            new Book(1, "9780385534635", "The Night Circus", "Erin Morgenstern", "Fiction",
                    new BigDecimal("3250"), 7,
                    "A mysterious circus opens only at night, drawing visitors into a world of illusion, rivalry, and unexpected love.",
                    "https://covers.openlibrary.org/b/id/8773134-M.jpg?default=false"),
            new Book(2, "9781455563920", "Pachinko", "Min Jin Lee", "Fiction",
                    new BigDecimal("3850"), 5,
                    "A sweeping family story following generations of a Korean family as they build their lives in Japan.",
                    "https://covers.openlibrary.org/b/id/8044605-M.jpg?default=false"),
            new Book(3, "9780132350884", "Clean Code", "Robert C. Martin", "Technology",
                    new BigDecimal("5200"), 4,
                    "A practical guide to writing code that is clearer, easier to maintain, and more pleasant to work with.",
                    "https://covers.openlibrary.org/b/id/8065615-M.jpg?default=false"),
            new Book(4, "9780857197689", "The Psychology of Money", "Morgan Housel", "Business",
                    new BigDecimal("3600"), 9,
                    "Short, thoughtful lessons on how behavior and personal experience shape decisions about money.",
                    "https://covers.openlibrary.org/b/id/10389354-M.jpg?default=false"),
            new Book(5, "9780735211292", "Atomic Habits", "James Clear", "Personal Development",
                    new BigDecimal("3400"), 8,
                    "An approachable look at how small, consistent changes can help build better habits over time.",
                    "https://covers.openlibrary.org/b/id/12539702-M.jpg?default=false"),
            new Book(6, "9780553380163", "A Brief History of Time", "Stephen Hawking", "Science",
                    new BigDecimal("3100"), 6,
                    "An introduction to big questions about the universe, from space and time to black holes and cosmology.",
                    "https://covers.openlibrary.org/b/id/10432365-M.jpg?default=false"),
            new Book(7, "9780062316097", "Sapiens", "Yuval Noah Harari", "Science",
                    new BigDecimal("4250"), 3,
                    "A wide-ranging exploration of human history and the ideas that helped shape societies.",
                    "https://covers.openlibrary.org/b/id/8634250-M.jpg?default=false"),
            new Book(8, "9780671212094", "How to Read a Book", "Mortimer J. Adler & Charles Van Doren", "Education",
                    new BigDecimal("2950"), 0,
                    "A guide to reading more actively and getting more from books of different kinds.",
                    "https://covers.openlibrary.org/b/id/405381-M.jpg?default=false")
        );
    }

    public List<Book> getAllBooks() {
        return repository.readAll();
    }

    public Optional<Book> findById(long id) {
        return getAllBooks().stream().filter(book -> book.getId() == id).findFirst();
    }

    public Optional<Book> getBookById(long id) {
        return findById(id);
    }

    public List<String> getCategories() {
        return getAllBooks().stream().map(Book::getCategory).distinct().toList();
    }

    public List<Book> findBooks(String query, String category, String sort) {
        String searchText = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String selectedCategory = category == null ? "" : category.trim();

        Comparator<Book> order = switch (sort == null ? "" : sort) {
            case "title-desc" -> Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
            case "price-asc" -> Comparator.comparing(Book::getPrice);
            case "price-desc" -> Comparator.comparing(Book::getPrice).reversed();
            default -> Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
        };

        return getAllBooks().stream()
                .filter(book -> searchText.isEmpty()
                        || book.getTitle().toLowerCase(Locale.ROOT).contains(searchText)
                        || book.getAuthor().toLowerCase(Locale.ROOT).contains(searchText))
                .filter(book -> selectedCategory.isEmpty()
                        || book.getCategory().equalsIgnoreCase(selectedCategory))
                .sorted(order)
                .toList();
    }

    public synchronized Book addBook(Book book) {
        return addBook(book, book.getFormat());
    }

    // Keep the read and save together for requests in this app instance.
    public synchronized Book addBook(Book book, String format) {
        List<Book> books = new ArrayList<>(repository.readAll());
        Map<String, String> errors = validateBook(book, null, books);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.values().iterator().next());
        }
        long nextId = books.stream().mapToLong(Book::getId).max().orElse(0) + 1;
        if (nextId <= 0) {
            throw new IllegalStateException("No more book IDs are available.");
        }
        Book newBook = copyWithId(book, nextId, format);
        books.add(newBook);
        repository.saveAll(books);
        return newBook;
    }

    public synchronized void updateBook(long id, Book changes) {
        List<Book> books = new ArrayList<>(repository.readAll());
        int index = indexOf(books, id);
        if (index < 0) {
            throw new IllegalArgumentException("The book no longer exists.");
        }
        // A plain Book from older code keeps the existing stored format on edit.
        String format = changes.getClass() == Book.class ? books.get(index).getFormat() : changes.getFormat();
        saveUpdatedBook(books, index, id, changes, format);
    }

    public synchronized void updateBook(long id, Book changes, String format) {
        List<Book> books = new ArrayList<>(repository.readAll());
        int index = indexOf(books, id);
        if (index < 0) {
            throw new IllegalArgumentException("The book no longer exists.");
        }
        saveUpdatedBook(books, index, id, changes, format);
    }

    private void saveUpdatedBook(List<Book> books, int index, long id, Book changes, String format) {
        Map<String, String> errors = validateBook(changes, id, books);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.values().iterator().next());
        }
        books.set(index, copyWithId(changes, id, format));
        repository.saveAll(books);
    }

    public synchronized void deleteBook(long id) {
        List<Book> books = new ArrayList<>(repository.readAll());
        int index = indexOf(books, id);
        if (index < 0) {
            throw new IllegalArgumentException("The book no longer exists.");
        }
        books.remove(index);
        repository.saveAll(books);
    }

    public Map<String, String> validateBook(Book book, Long currentId) {
        return validateBook(book, currentId, repository.readAll());
    }

    public boolean isSupportedFormat(String format) {
        return "Printed Book".equals(format) || "E-Book".equals(format);
    }

    private Map<String, String> validateBook(Book book, Long currentId, List<Book> books) {
        Map<String, String> errors = new LinkedHashMap<>();
        String isbn = book.getIsbn() == null ? "" : book.getIsbn().trim();
        // An edited book may keep its own ISBN, but another book cannot use it.
        if (!isbn.matches("[0-9]{10}|[0-9]{13}")) {
            errors.put("isbn", "ISBN must contain exactly 10 or 13 digits.");
        } else if (books.stream().anyMatch(existing -> existing.getIsbn().equals(isbn)
                && (currentId == null || existing.getId() != currentId))) {
            errors.put("isbn", "Another book already uses this ISBN.");
        }
        required(book.getTitle(), "title", "Title", 150, errors);
        required(book.getAuthor(), "author", "Author", 120, errors);
        required(book.getCategory(), "category", "Category", 80, errors);
        String description = book.getDescription() == null ? "" : book.getDescription().trim();
        if (description.length() < 10 || description.length() > 1000) {
            errors.put("description", "Description must be between 10 and 1000 characters.");
        }
        if (book.getPrice() == null || book.getPrice().signum() <= 0) {
            errors.put("price", "Price must be greater than zero.");
        }
        if (book.getStock() < 0) {
            errors.put("stock", "Stock cannot be negative.");
        }
        String coverUrl = book.getCoverUrl() == null ? "" : book.getCoverUrl().trim();
        if (!coverUrl.isEmpty()) {
            try {
                URI uri = URI.create(coverUrl);
                if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null || coverUrl.length() > 500) {
                    errors.put("coverUrl", "Cover URL must be a valid http or https address (500 characters maximum).");
                }
            } catch (IllegalArgumentException exception) {
                errors.put("coverUrl", "Cover URL must be a valid http or https address.");
            }
        }
        return errors;
    }

    private void required(String value, String field, String label, int maxLength, Map<String, String> errors) {
        if (value == null || value.isBlank()) {
            errors.put(field, label + " is required.");
        } else if (value.trim().length() > maxLength) {
            errors.put(field, label + " must be " + maxLength + " characters or fewer.");
        }
    }

    private int indexOf(List<Book> books, long id) {
        for (int index = 0; index < books.size(); index++) {
            if (books.get(index).getId() == id) {
                return index;
            }
        }
        return -1;
    }

    // A new object keeps the saved ID and the selected book type.
    private Book copyWithId(Book book, long id, String format) {
        String isbn = book.getIsbn().trim();
        String title = book.getTitle().trim();
        String author = book.getAuthor().trim();
        String category = book.getCategory().trim();
        String description = book.getDescription().trim();
        String coverUrl = book.getCoverUrl() == null ? "" : book.getCoverUrl().trim();
        return switch (format == null ? "" : format) {
            case "Printed Book" -> new PrintedBook(id, isbn, title, author, category,
                    book.getPrice(), book.getStock(), description, coverUrl);
            case "E-Book" -> new EBook(id, isbn, title, author, category,
                    book.getPrice(), book.getStock(), description, coverUrl);
            default -> throw new IllegalArgumentException("Choose Printed Book or E-Book.");
        };
    }
}
