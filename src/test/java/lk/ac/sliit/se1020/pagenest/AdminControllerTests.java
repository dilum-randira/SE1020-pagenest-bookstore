package lk.ac.sliit.se1020.pagenest;

import jakarta.servlet.http.HttpServletResponse;
import lk.ac.sliit.se1020.pagenest.controller.AdminController;
import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AdminControllerTests {

    private final BookService bookService = new BookService();
    private final AdminController controller = new AdminController(bookService);

    @Test
    void dashboardUsesTheSharedSampleCollection() {
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("admin/preview-dashboard", controller.dashboard(model));
        assertEquals(8, model.get("bookCount"));
        assertEquals(42, model.get("stockUnits"));
        assertEquals(new BigDecimal("153750"), model.get("inventoryValue"));
        assertEquals(4L, model.get("lowStockCount"));
        assertEquals(5, ((List<?>) model.get("selectedBooks")).size());
    }

    @Test
    void inventorySearchesIsbnAndFiltersCategory() {
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("admin/preview-books", controller.books("9780132350884", "Technology", model));
        List<?> books = (List<?>) model.get("books");
        assertEquals(1, books.size());
        assertEquals("Clean Code", ((Book) books.getFirst()).getTitle());

        controller.books("no matching title", "", model);
        assertEquals(0, ((List<?>) model.get("books")).size());
    }

    @Test
    void editPreviewDoesNotChangeBookDataAndMissingIdReturns404() {
        ExtendedModelMap model = new ExtendedModelMap();
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertEquals("admin/preview-book-form", controller.editBook("1", model, response));
        assertEquals("The Night Circus", ((Book) model.get("book")).getTitle());

        assertEquals("admin/preview-not-found", controller.editBook("unknown", model, response));
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        assertEquals(8, bookService.getAllBooks().size());
        assertFalse(bookService.findById(1).isEmpty());
    }
}
