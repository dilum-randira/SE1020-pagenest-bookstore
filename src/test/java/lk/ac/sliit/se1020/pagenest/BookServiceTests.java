package lk.ac.sliit.se1020.pagenest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import lk.ac.sliit.se1020.pagenest.service.BookService;

class BookServiceTests {

    private final BookService bookService = new BookService();

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
}
