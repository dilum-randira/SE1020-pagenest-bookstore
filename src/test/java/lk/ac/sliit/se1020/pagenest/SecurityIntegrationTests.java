package lk.ac.sliit.se1020.pagenest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "PAGENEST_ADMIN_PASSWORD=test-only-admin-password",
        "PAGENEST_CUSTOMER_PASSWORD=test-only-customer-password"
})
@AutoConfigureMockMvc
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void publicPagesAndStylesRemainAvailable() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("PageNest")));
        mvc.perform(get("/books")).andExpect(status().isOk());
        mvc.perform(get("/books/1")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/")).andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/login\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Admin Dashboard"))));
        mvc.perform(get("/css/style.css")).andExpect(status().isOk());
        mvc.perform(get("/css/admin.css")).andExpect(status().isOk());
    }

    @Test
    void anonymousVisitorsAreSentToLogin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
        mvc.perform(get("/admin/books")).andExpect(status().is3xxRedirection());
    }

    @Test
    void adminCanLogInAndReadProtectedPages() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user("admin").password("test-only-admin-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername("admin")).andReturn().getRequest().getSession(false);
        mvc.perform(get("/admin").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Inventory summary")));
        mvc.perform(get("/admin/books").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Clean Code")));
        mvc.perform(get("/admin/books/new").session(session)).andExpect(status().isOk());
        mvc.perform(get("/admin/books/1/edit").session(session)).andExpect(status().isOk());
        mvc.perform(get("/").session(session)).andExpect(content().string(org.hamcrest.Matchers.containsString("Admin Dashboard")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
    }

    @Test
    void adminReturnsToRequestedProtectedPageAfterLogin() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(get("/admin/books"))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession(false);
        mvc.perform(post("/login").session(session).with(csrf())
                        .param("username", "admin").param("password", "test-only-admin-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/admin/books")));
    }

    @Test
    void customerCanLogInButGets403OnAdminPages() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user("customer").password("test-only-customer-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/books"))
                .andExpect(authenticated().withUsername("customer")).andReturn().getRequest().getSession(false);
        mvc.perform(get("/books").session(session)).andExpect(status().isOk());
        mvc.perform(get("/").session(session))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Admin Dashboard"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Logout")));
        mvc.perform(get("/admin").session(session)).andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
        mvc.perform(get("/admin/books").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/access-denied").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This shelf is for admins")));
    }

    @Test
    void wrongPasswordIsRejectedAndCsrfIsRequired() throws Exception {
        mvc.perform(formLogin().user("admin").password("wrong-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
        mvc.perform(post("/login").param("username", "admin").param("password", "test-only-admin-password"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/logout")).andExpect(status().isForbidden());
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user("admin").password("test-only-admin-password"))
                .andReturn().getRequest().getSession(false);
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
        mvc.perform(get("/admin").session(session)).andExpect(status().is3xxRedirection());
    }

    @Test
    void oldPreviewRoutesAndBookWritesAreUnavailable() throws Exception {
        mvc.perform(get("/preview/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/preview/admin/books").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/books").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }
}
