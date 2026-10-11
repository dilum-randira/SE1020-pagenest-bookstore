package lk.ac.sliit.se1020.pagenest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "PAGENEST_ADMIN_PASSWORD=test-only-admin-password",
        "PAGENEST_CUSTOMER_PASSWORD=test-only-customer-password"
})
class PageNestApplicationTests {

    @Test
    void contextLoads() {
    }
}
