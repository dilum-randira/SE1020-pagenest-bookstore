package lk.ac.sliit.se1020.pagenest;

import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "PAGENEST_ADMIN_PASSWORD=test-only-admin-password",
        "PAGENEST_CUSTOMER_PASSWORD=test-only-customer-password",
        "pagenest.books.file=target/test-data/crud-${random.uuid}/books.txt"
})
@AutoConfigureMockMvc
class BookCrudIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookService books;

    @Test
    void adminCreateUpdateDeleteChangesPublicPagesAndDashboard() throws Exception {
        mvc.perform(post("/admin/books").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("isbn", "9781234567890").param("title", "New Test Book")
                        .param("author", "Test Author").param("category", "Fiction")
                        .param("price", "1200").param("stock", "2")
                        .param("description", "A test book for the catalog.").param("coverUrl", ""))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/books"));
        Book created = books.getAllBooks().stream().filter(book -> book.getIsbn().equals("9781234567890"))
                .findFirst().orElseThrow();
        long id = created.getId();
        mvc.perform(get("/books")).andExpect(content().string(org.hamcrest.Matchers.containsString("New Test Book")));
        mvc.perform(get("/books/" + id)).andExpect(status().isOk());
        mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(model().attribute("bookCount", 9));

        mvc.perform(post("/admin/books/{id}/edit", id).with(user("admin").roles("ADMIN")).with(csrf())
                        .param("isbn", "9781234567890").param("title", "Updated Test Book")
                        .param("author", "Test Author").param("category", "Science")
                        .param("price", "1500").param("stock", "4")
                        .param("description", "An updated test description.").param("coverUrl", ""))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/books"));
        assertEquals(id, books.findById(id).orElseThrow().getId());
        mvc.perform(get("/books/" + id)).andExpect(content().string(org.hamcrest.Matchers.containsString("Updated Test Book")));
        mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(model().attribute("inventoryValue", new BigDecimal("159750")));

        mvc.perform(post("/admin/books/{id}/delete", id).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/books"));
        assertFalse(books.findById(id).isPresent());
        mvc.perform(get("/books/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/books")).andExpect(content().string(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString("Updated Test Book"))));
    }

    @Test
    void writesRequireAdminRoleAndCsrf() throws Exception {
        int originalCount = books.getAllBooks().size();
        mvc.perform(post("/admin/books").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/books/1/edit").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/books/1/delete").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/books").with(user("customer").roles("CUSTOMER")).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/books").with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(post("/admin/books/1/edit").with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(post("/admin/books/1/delete").with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(post("/admin/books/1/edit").with(user("customer").roles("CUSTOMER")).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/books/1/delete").with(user("customer").roles("CUSTOMER")).with(csrf()))
                .andExpect(status().isForbidden());
        assertEquals(originalCount, books.getAllBooks().size());
    }

    @Test
    void invalidAndDuplicateInputKeepsFormData() throws Exception {
        mvc.perform(post("/admin/books").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("isbn", "9780385534635").param("title", "Kept Title")
                        .param("author", "Test Author").param("category", "Fiction")
                        .param("price", "0").param("stock", "-1")
                        .param("description", "A valid test description.").param("coverUrl", "bad-url"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("book", "isbn", "price", "stock", "coverUrl"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Kept Title")));
    }
}
