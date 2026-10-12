package lk.ac.sliit.se1020.pagenest.controller;

import jakarta.servlet.http.HttpServletResponse;
import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private final BookService bookService;

    public AdminController(BookService bookService) {
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
        model.addAttribute("selectedFormat", "");
        prepareForm(model, false);
        return "admin/preview-book-form";
    }

    @PostMapping("/books")
    public String addBook(@ModelAttribute("book") Book book, BindingResult binding,
                          @RequestParam(defaultValue = "") String format,
                          Model model, RedirectAttributes redirect) {
        model.addAttribute("selectedFormat", format);
        if (!bookService.isSupportedFormat(format)) {
            binding.reject("format", "Choose Printed Book or E-Book.");
        }
        addValidationErrors(book, null, binding);
        if (binding.hasErrors()) {
            prepareForm(model, false);
            return "admin/preview-book-form";
        }
        try {
            bookService.addBook(book, format);
            redirect.addFlashAttribute("success", "Book added successfully.");
            return "redirect:/admin/books";
        } catch (IllegalArgumentException | IllegalStateException | UncheckedIOException exception) {
            prepareForm(model, false);
            model.addAttribute("formError", exception.getMessage());
            return "admin/preview-book-form";
        }
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
        model.addAttribute("selectedFormat", book.getFormat());
        prepareForm(model, true);
        return "admin/preview-book-form";
    }

    @PostMapping("/books/{id}/edit")
    public String updateBook(@PathVariable long id, @ModelAttribute("book") Book book,
                             BindingResult binding, @RequestParam(defaultValue = "") String format,
                             Model model, RedirectAttributes redirect) {
        book.setId(id);
        model.addAttribute("selectedFormat", format);
        if (!bookService.isSupportedFormat(format)) {
            binding.reject("format", "Choose Printed Book or E-Book.");
        }
        addValidationErrors(book, id, binding);
        if (binding.hasErrors()) {
            prepareForm(model, true);
            return "admin/preview-book-form";
        }
        try {
            bookService.updateBook(id, book, format);
            redirect.addFlashAttribute("success", "Book updated successfully.");
            return "redirect:/admin/books";
        } catch (IllegalArgumentException | IllegalStateException | UncheckedIOException exception) {
            prepareForm(model, true);
            model.addAttribute("formError", exception.getMessage());
            return "admin/preview-book-form";
        }
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable long id, RedirectAttributes redirect) {
        try {
            bookService.deleteBook(id);
            redirect.addFlashAttribute("success", "Book deleted successfully.");
        } catch (IllegalArgumentException | IllegalStateException | UncheckedIOException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/books";
    }

    private void prepareForm(Model model, boolean editing) {
        model.addAttribute("editing", editing);
        try {
            model.addAttribute("categories", bookService.getCategories());
        } catch (IllegalStateException | UncheckedIOException exception) {
            // Keep the entered form visible even if its category suggestions cannot be loaded.
            model.addAttribute("categories", List.of());
            if (!model.containsAttribute("formError")) {
                model.addAttribute("formError", exception.getMessage());
            }
        }
    }

    private void addValidationErrors(Book book, Long currentId, BindingResult binding) {
        try {
            for (Map.Entry<String, String> error : bookService.validateBook(book, currentId).entrySet()) {
                if (!binding.hasFieldErrors(error.getKey())) {
                    binding.rejectValue(error.getKey(), "invalid", error.getValue());
                }
            }
        } catch (IllegalStateException | UncheckedIOException exception) {
            binding.reject("storage", exception.getMessage());
        }
    }
}
