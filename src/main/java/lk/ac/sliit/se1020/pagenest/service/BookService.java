package lk.ac.sliit.se1020.pagenest.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lk.ac.sliit.se1020.pagenest.model.Book;

@Service
public class BookService {

    // Sample data for Milestone 3. A later milestone will replace this with books.txt.
    private final List<Book> books = List.of(
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

    public List<Book> getAllBooks() {
        return books;
    }

    public Optional<Book> findById(long id) {
        return books.stream().filter(book -> book.getId() == id).findFirst();
    }

    public List<String> getCategories() {
        return books.stream().map(Book::getCategory).distinct().toList();
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

        return books.stream()
                .filter(book -> searchText.isEmpty()
                        || book.getTitle().toLowerCase(Locale.ROOT).contains(searchText)
                        || book.getAuthor().toLowerCase(Locale.ROOT).contains(searchText))
                .filter(book -> selectedCategory.isEmpty()
                        || book.getCategory().equalsIgnoreCase(selectedCategory))
                .sorted(order)
                .toList();
    }
}
