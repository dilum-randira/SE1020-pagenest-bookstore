package lk.ac.sliit.se1020.pagenest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.repository.BookFileRepository;
import lk.ac.sliit.se1020.pagenest.service.BookService;

class BookServiceTests {

    @TempDir
    Path directory;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(new BookFileRepository(directory.resolve("books.txt")));
    }

    @Test
    void containsEightBooksWithUniqueIds() {
        assertEquals(8, bookService.getAllBooks().size());
        assertEquals(8, bookService.getAllBooks().stream().map(book -> book.getId()).distinct().count());
        assertEquals("The Night Circus", bookService.findById(1).orElseThrow().getTitle());
        assertTrue(bookService.findById(999).isEmpty());
    }

    @Test
    void searchesTitleAndAuthorWithoutCaseSensitivity() {
        assertEquals("Pachinko", bookService.findBooks("PACHINKO", "", "title-asc").getFirst().getTitle());
        assertEquals("Atomic Habits", bookService.findBooks("james clear", "", "title-asc").getFirst().getTitle());
        assertTrue(bookService.findBooks("does not exist", "", "title-asc").isEmpty());
    }

    @Test
    void filtersAndSortsBooks() {
        assertEquals(2, bookService.findBooks("", "Fiction", "title-asc").size());
        assertEquals("How to Read a Book", bookService.findBooks("", "", "price-asc").getFirst().getTitle());
        assertEquals(new BigDecimal("5200"), bookService.findBooks("", "", "price-desc").getFirst().getPrice());
        assertEquals("The Psychology of Money", bookService.findBooks("", "", "title-desc").getFirst().getTitle());
    }

    @Test
    void createUpdateDeleteAndReloadKeepStableIds() {
        Book newBook = new Book(0, "9781234567890", "A New Shelf", "Test Author", "Fiction",
                new BigDecimal("1200"), 2, "A test description with a tab\tand a new line\nplus a quote \"here\".", "");
        long id = bookService.addBook(newBook).getId();
        assertEquals(9, id);
        assertEquals(9, new BookService(new BookFileRepository(directory.resolve("books.txt"))).getAllBooks().size());

        newBook.setTitle("An Updated Shelf");
        bookService.updateBook(id, newBook);
        BookService reloaded = new BookService(new BookFileRepository(directory.resolve("books.txt")));
        assertEquals("An Updated Shelf", reloaded.findById(id).orElseThrow().getTitle());
        assertEquals(newBook.getDescription(), reloaded.findById(id).orElseThrow().getDescription());
        assertEquals(id, reloaded.findById(id).orElseThrow().getId());

        reloaded.deleteBook(id);
        assertFalse(new BookService(new BookFileRepository(directory.resolve("books.txt"))).findById(id).isPresent());
    }

    @Test
    void rejectsDuplicateIsbnInvalidFieldsAndMissingIds() {
        Book duplicate = new Book(0, "9780385534635", "Another Title", "Test Author", "Fiction",
                new BigDecimal("1200"), 1, "A valid description.", "");
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(duplicate));
        duplicate.setIsbn("invalid");
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(duplicate));
        duplicate.setIsbn("9781234567890");
        duplicate.setPrice(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(duplicate));
        duplicate.setPrice(new BigDecimal("1200"));
        duplicate.setStock(-1);
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(duplicate));
        duplicate.setStock(1);
        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(999, duplicate));
        assertThrows(IllegalArgumentException.class, () -> bookService.deleteBook(999));
        assertEquals(8, bookService.getAllBooks().size());
    }
}
