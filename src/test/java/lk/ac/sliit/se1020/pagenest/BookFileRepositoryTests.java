package lk.ac.sliit.se1020.pagenest;

import lk.ac.sliit.se1020.pagenest.model.Book;
import lk.ac.sliit.se1020.pagenest.model.PrintedBook;
import lk.ac.sliit.se1020.pagenest.repository.BookFileRepository;
import lk.ac.sliit.se1020.pagenest.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookFileRepositoryTests {

    @TempDir
    Path directory;

    @Test
    void existingFileIsLoadedWithoutReseeding() throws Exception {
        Path file = directory.resolve("books.txt");
        BookService service = new BookService(new BookFileRepository(file));
        service.deleteBook(1);
        String savedText = Files.readString(file, StandardCharsets.UTF_8);

        BookService reloaded = new BookService(new BookFileRepository(file));
        assertEquals(7, reloaded.getAllBooks().size());
        assertEquals(savedText, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void malformedDataIsRejectedAndNotOverwritten() throws Exception {
        Path file = directory.resolve("books.txt");
        new BookService(new BookFileRepository(file));
        Files.writeString(file, Files.readString(file, StandardCharsets.UTF_8) + "broken-record\n",
                StandardCharsets.UTF_8);
        String damagedText = Files.readString(file, StandardCharsets.UTF_8);

        assertThrows(IllegalStateException.class, () -> new BookService(new BookFileRepository(file)));
        assertEquals(damagedText, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void fileWriteFailureIsReported() throws Exception {
        Path parentFile = directory.resolve("not-a-folder");
        Files.writeString(parentFile, "keep me", StandardCharsets.UTF_8);
        BookFileRepository repository = new BookFileRepository(parentFile.resolve("books.txt"));
        assertThrows(UncheckedIOException.class, () -> repository.saveAll(java.util.List.of()));
        assertEquals("keep me", Files.readString(parentFile, StandardCharsets.UTF_8));
    }

    @Test
    void legacyNineFieldFileLoadsAsPrintedWithoutChangingTheFile() throws Exception {
        Path file = directory.resolve("books.txt");
        String legacy = "# PageNest books v1: id, isbn, title, author, category, price, stock, description, coverUrl (tab-separated)\n"
                + "12\t9781234567890\tLegacy Title\tTest Author\tFiction\t1200\t2\tA legacy\\tdescription with a \"quote\".\t\n";
        Files.writeString(file, legacy, StandardCharsets.UTF_8);

        Book book = new BookService(new BookFileRepository(file)).findById(12).orElseThrow();
        assertInstanceOf(PrintedBook.class, book);
        assertEquals("Printed Book", book.getFormat());
        assertEquals("A legacy\tdescription with a \"quote\".", book.getDescription());
        assertEquals(legacy, Files.readString(file, StandardCharsets.UTF_8));

        new BookService(new BookFileRepository(file)).updateBook(12, book);
        assertEquals("Printed Book", new BookService(new BookFileRepository(file))
                .findById(12).orElseThrow().getFormat());
        assertTrue(Files.readString(file, StandardCharsets.UTF_8).startsWith("# PageNest books v2:"));
    }

    @Test
    void unknownFormatAndWrongFieldCountAreRejectedWithoutRewriting() throws Exception {
        Path file = directory.resolve("books.txt");
        String header = "# PageNest books v2: id, isbn, title, author, category, price, stock, description, coverUrl, format (tab-separated)\n";
        for (String line : java.util.List.of(
                "12\t9781234567890\tTitle\tAuthor\tFiction\t1200\t2\tA description.\t\tUnknown\n",
                "12\t9781234567890\tTitle\tAuthor\tFiction\t1200\t2\tA description.\t\n")) {
            String damaged = header + line;
            Files.writeString(file, damaged, StandardCharsets.UTF_8);
            assertThrows(IllegalStateException.class, () -> new BookService(new BookFileRepository(file)));
            assertEquals(damaged, Files.readString(file, StandardCharsets.UTF_8));
        }
    }
}
