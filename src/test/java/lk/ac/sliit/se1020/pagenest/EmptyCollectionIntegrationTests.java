package lk.ac.sliit.se1020.pagenest;

import lk.ac.sliit.se1020.pagenest.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "PAGENEST_ADMIN_PASSWORD=test-only-admin-password",
        "PAGENEST_CUSTOMER_PASSWORD=test-only-customer-password",
        "pagenest.books.file=target/test-data/empty-${random.uuid}/books.txt"
})
@AutoConfigureMockMvc
class EmptyCollectionIntegrationTests {

    @Autowired
    private BookService books;

    @Autowired
    private MockMvc mvc;

    @Test
    void homepageAndDashboardRenderAfterAllBooksAreDeleted() throws Exception {
        books.getAllBooks().forEach(book -> books.deleteBook(book.getId()));
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No books are available yet")));
        mvc.perform(get("/books")).andExpect(status().isOk());
        mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(model().attribute("bookCount", 0))
                .andExpect(model().attribute("stockUnits", 0));
    }
}
