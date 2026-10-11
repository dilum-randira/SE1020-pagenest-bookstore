package lk.ac.sliit.se1020.pagenest;

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
}
