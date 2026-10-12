package lk.ac.sliit.se1020.pagenest.model;

import java.math.BigDecimal;

public class EBook extends Book {

    public EBook(long id, String isbn, String title, String author, String category,
                 BigDecimal price, int stock, String description, String coverUrl) {
        super(id, isbn, title, author, category, price, stock, description, coverUrl);
    }

    // A Book reference calls this version when it holds an EBook.
    @Override
    public String getFormat() {
        return "E-Book";
    }
}
