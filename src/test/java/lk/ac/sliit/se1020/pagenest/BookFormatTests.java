package lk.ac.sliit.se1020.pagenest;

import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.model.EBook;
import lk.ac.sliit.se1020.pagenest.model.PrintedBook;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BookFormatTests {

    @Test
    void bookReferencesUseTheSubclassFormatMethods() {
        Book printed = new PrintedBook(1, "9781234567890", "Printed Title", "Author", "Fiction",
                new BigDecimal("1000"), 2, "Printed description.", "");
        Book electronic = new EBook(2, "9781234567891", "Digital Title", "Author", "Fiction",
                new BigDecimal("900"), 1, "Digital description.", "");

        assertInstanceOf(Book.class, printed);
        assertInstanceOf(Book.class, electronic);
        assertEquals("Printed Book", printed.getFormat());
        assertEquals("E-Book", electronic.getFormat());
        assertEquals("Printed Book", new Book().getFormat());
    }
}
