package lk.ac.sliit.se1020.pagenest.controller;

import jakarta.servlet.http.HttpServletResponse;
import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public String showCatalog(@RequestParam(defaultValue = "") String q,
                              @RequestParam(defaultValue = "") String category,
                              @RequestParam(defaultValue = "title-asc") String sort,
                              Model model) {
        model.addAttribute("books", bookService.findBooks(q, category, sort));
        model.addAttribute("categories", bookService.getCategories());
        model.addAttribute("query", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSort", sort);
        return "books";
    }

    @GetMapping("/books/{id}")
    public String showBook(@PathVariable String id, Model model, HttpServletResponse response) {
        Book book;
        try {
            book = bookService.findById(Long.parseLong(id)).orElse(null);
        } catch (NumberFormatException exception) {
            book = null;
        }

        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "book-not-found";
        }

        model.addAttribute("book", book);
        return "book-details";
    }
}
