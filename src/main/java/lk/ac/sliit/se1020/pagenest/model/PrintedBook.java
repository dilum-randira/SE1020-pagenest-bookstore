package lk.ac.sliit.se1020.pagenest.model;

import java.math.BigDecimal;

public class PrintedBook extends Book {

    public PrintedBook(long id, String isbn, String title, String author, String category,
                       BigDecimal price, int stock, String description, String coverUrl) {
        super(id, isbn, title, author, category, price, stock, description, coverUrl);
    }

    // This type uses Book's shared fields and identifies printed copies.
    @Override
    public String getFormat() {
        return "Printed Book";
    }
}
