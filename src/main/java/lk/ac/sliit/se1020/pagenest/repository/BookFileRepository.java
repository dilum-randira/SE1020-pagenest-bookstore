package lk.ac.sliit.se1020.pagenest.repository;

import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.model.EBook;
import lk.ac.sliit.se1020.pagenest.model.PrintedBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class BookFileRepository {

    private static final String HEADER_V1 = "# PageNest books v1: id, isbn, title, author, category, price, stock, description, coverUrl (tab-separated)";
    private static final String HEADER_V2 = "# PageNest books v2: id, isbn, title, author, category, price, stock, description, coverUrl, format (tab-separated)";
    private final Path file;

    @Autowired
    public BookFileRepository(@Value("${pagenest.books.file:data/books.txt}") String filePath) {
        this(Path.of(filePath));
    }

    public BookFileRepository(Path file) {
        this.file = file.toAbsolutePath().normalize();
    }

    public void initializeIfMissing(List<Book> demoBooks) {
        if (Files.exists(file)) {
            readAll(); // Refuse to continue if existing data is malformed.
            return;
        }
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = writeTemporaryFile(demoBooks);
            try {
                // No REPLACE_EXISTING: another process's file must never be overwritten during setup.
                Files.move(temporary, file);
            } catch (FileAlreadyExistsException exception) {
                readAll();
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not initialize the book data file.", exception);
        } finally {
            deleteTemporary(temporary);
        }
    }

    public List<Book> readAll() {
        if (!Files.exists(file)) {
            throw new IllegalStateException("The book data file is missing. Restart the application to initialize it.");
        }
        List<Book> books = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!HEADER_V1.equals(header) && !HEADER_V2.equals(header)) {
                throw new IllegalStateException("The book data file has an unsupported or damaged header.");
            }
            // Older nine-field records have no format, so they load as printed books.
            boolean legacy = HEADER_V1.equals(header);
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    books.add(parse(line, legacy));
                } catch (RuntimeException exception) {
                    throw new IllegalStateException("Invalid book data on line " + lineNumber + ". No records were changed.", exception);
                }
            }
            checkUniqueRecords(books);
            return books;
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read the book data file.", exception);
        }
    }

    public void saveAll(List<Book> books) {
        checkUniqueRecords(books);
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = writeTemporaryFile(books);
            // Replace the saved file only after the whole new file has been written.
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not save the book data file. No success was recorded.", exception);
        } finally {
            deleteTemporary(temporary);
        }
    }

    private Path writeTemporaryFile(List<Book> books) throws IOException {
        Path temporary = Files.createTempFile(file.getParent(), ".books-", ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            writer.write(HEADER_V2);
            writer.newLine();
            for (Book book : books) {
                writer.write(String.join("\t", Long.toString(book.getId()), escape(book.getIsbn()),
                        escape(book.getTitle()), escape(book.getAuthor()), escape(book.getCategory()),
                        book.getPrice().toPlainString(), Integer.toString(book.getStock()),
                        escape(book.getDescription()), escape(book.getCoverUrl()), book.getFormat()));
                writer.newLine();
            }
            return temporary;
        } catch (IOException | RuntimeException exception) {
            deleteTemporary(temporary);
            throw exception;
        }
    }

    private Book parse(String line, boolean legacy) {
        String[] values = line.split("\t", -1);
        if (values.length != (legacy ? 9 : 10)) {
            throw new IllegalArgumentException("Incorrect number of fields.");
        }
        long id = Long.parseLong(values[0]);
        String isbn = unescape(values[1]);
        String title = unescape(values[2]);
        String author = unescape(values[3]);
        String category = unescape(values[4]);
        BigDecimal price = new BigDecimal(values[5]);
        int stock = Integer.parseInt(values[6]);
        String description = unescape(values[7]);
        String coverUrl = unescape(values[8]);
        String format = legacy ? "Printed Book" : values[9];
        Book book = switch (format) {
            case "Printed Book" -> new PrintedBook(id, isbn, title, author, category, price, stock, description, coverUrl);
            case "E-Book" -> new EBook(id, isbn, title, author, category, price, stock, description, coverUrl);
            default -> throw new IllegalArgumentException("Unknown book format.");
        };
        if (book.getId() <= 0 || book.getIsbn().isBlank() || book.getTitle().isBlank()
                || book.getAuthor().isBlank() || book.getCategory().isBlank()
                || book.getPrice().signum() <= 0 || book.getStock() < 0) {
            throw new IllegalArgumentException("Invalid book fields.");
        }
        return book;
    }

    private void checkUniqueRecords(List<Book> books) {
        Set<Long> ids = new HashSet<>();
        Set<String> isbns = new HashSet<>();
        for (Book book : books) {
            if (book == null || book.getId() <= 0 || !ids.add(book.getId())
                    || book.getIsbn() == null || !isbns.add(book.getIsbn())
                    || !(book.getFormat().equals("Printed Book") || book.getFormat().equals("E-Book"))) {
                throw new IllegalStateException("The book data contains duplicate or invalid records. No records were changed.");
            }
        }
    }

    // Backslashes protect tabs and line breaks; quotes and ordinary text remain readable.
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\t", "\\t")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private String unescape(String value) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character != '\\') {
                result.append(character);
                continue;
            }
            if (++index == value.length()) {
                throw new IllegalArgumentException("Incomplete escape.");
            }
            result.append(switch (value.charAt(index)) {
                case '\\' -> '\\';
                case 't' -> '\t';
                case 'n' -> '\n';
                case 'r' -> '\r';
                default -> throw new IllegalArgumentException("Unknown escape.");
            });
        }
        return result.toString();
    }

    private void deleteTemporary(Path temporary) {
        if (temporary != null) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // The original data file is still untouched if cleanup fails.
            }
        }
    }
}
