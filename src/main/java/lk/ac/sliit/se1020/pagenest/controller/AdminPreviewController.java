package lk.ac.sliit.se1020.pagenest.controller;

import jakarta.servlet.http.HttpServletResponse;
import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/preview/admin")
public class AdminPreviewController {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private final BookService bookService;

    public AdminPreviewController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("bookCount", books.size());
        model.addAttribute("stockUnits", books.stream().mapToInt(Book::getStock).sum());
        model.addAttribute("inventoryValue", books.stream()
                .map(book -> book.getPrice().multiply(BigDecimal.valueOf(book.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("lowStockCount", books.stream()
                .filter(book -> book.getStock() <= LOW_STOCK_THRESHOLD).count());
        model.addAttribute("lowStockThreshold", LOW_STOCK_THRESHOLD);
        model.addAttribute("selectedBooks", books.stream().limit(5).toList());
        return "admin/preview-dashboard";
    }

    @GetMapping("/books")
    public String books(@RequestParam(defaultValue = "") String q,
                        @RequestParam(defaultValue = "") String category,
                        Model model) {
        String search = q.trim().toLowerCase(Locale.ROOT);
        List<Book> matchingBooks = bookService.getAllBooks().stream()
                .filter(book -> search.isEmpty()
                        || book.getTitle().toLowerCase(Locale.ROOT).contains(search)
                        || book.getAuthor().toLowerCase(Locale.ROOT).contains(search)
                        || book.getIsbn().toLowerCase(Locale.ROOT).contains(search))
                .filter(book -> category.isBlank() || book.getCategory().equalsIgnoreCase(category))
                .toList();

        model.addAttribute("books", matchingBooks);
        model.addAttribute("categories", bookService.getCategories());
        model.addAttribute("query", q);
        model.addAttribute("selectedCategory", category);
        return "admin/preview-books";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("editing", false);
        model.addAttribute("categories", bookService.getCategories());
        return "admin/preview-book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable String id, Model model, HttpServletResponse response) {
        Book book;
        try {
            book = bookService.findById(Long.parseLong(id)).orElse(null);
        } catch (NumberFormatException exception) {
            book = null;
        }
        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "admin/preview-not-found";
        }
        model.addAttribute("book", book);
        model.addAttribute("editing", true);
        model.addAttribute("categories", bookService.getCategories());
        return "admin/preview-book-form";
    }
}
