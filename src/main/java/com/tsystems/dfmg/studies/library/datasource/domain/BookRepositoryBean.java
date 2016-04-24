package com.tsystems.dfmg.studies.library.datasource.domain;

import com.tsystems.dfmg.studies.library.datasource.UserContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class BookRepositoryBean implements BookRepository {

    private static final Log LOGGER = LogFactory.getLog(BookRepositoryBean.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Long, BookDetailed> books = new LinkedHashMap<>();

    @Autowired
    private UserContext userContext;

    @Autowired
    private Function<BookDetailed, Book> reducer;

    private long index = 0;

    public BookDetailed get(long id) {
        try {

            lock.readLock().lock();

            BookDetailed result = books.get(id);
            if (result == null) {
                throw new IllegalArgumentException(String.format("no book with such id (%s) was found", id));
            }
            LOGGER.info(String.format("user %s requested info on book %s", userContext.getUser().getLogin(), id));
            return result;
        } finally {

            lock.readLock().unlock();
        }
    }

    public BookListPage getPage(int offset, int limit, String titleFilter) {

        if (offset < 1) {
            throw new IllegalArgumentException(String.format("offset (%s) cannot be less than 1", offset));
        }

        if ((limit < 1) || (limit > 100)) {
            throw new IllegalArgumentException(String.format("limit (%s) must be in [1, 100] bounds", limit));
        }

        try {

            lock.readLock().lock();

            List<BookDetailed> filtered = books.values()
                    .stream()
                    .parallel()
                    .filter(b -> (titleFilter == null) || (b.getTitle().contains(titleFilter)))
                    .collect(Collectors.toList());

            List<Book> contents = filtered
                    .stream()
                    .parallel()
                    .map(reducer)
                    .skip(offset - 1)
                    .limit(limit)
                    .collect(Collectors.toList());

            BookListPage result = new BookListPage();
            result.setOffset(offset);
            result.setTotal(filtered.size());
            result.getBooks().addAll(contents);
            LOGGER.info(String.format("user %s requested range info on books (offset: %s, limit %s, filter: %s)", userContext.getUser().getLogin(), offset, limit, titleFilter));
            return result;
        } finally {

            lock.readLock().unlock();
        }
    }

    public BookDetailed add(String title, String description, String author, int pages) {

        Objects.requireNonNull(title, "book title is missing");
        Objects.requireNonNull(author, "book author is missing");

        if (pages < 1) {
            throw new IllegalArgumentException(String.format("number of pages (%s) must be positive", pages));
        }

        try {

            lock.writeLock().lock();

            long id = index++;

            BookDetailed result = new BookDetailed();
            result.setId(id);
            result.setTitle(title);
            result.setDescription(description);
            result.setAuthor(author);
            result.setPages(pages);
            books.put(id, result);
            LOGGER.info(String.format("user %s added a new book %s (%s)", userContext.getUser().getLogin(), id, title));
            return result;
        } finally {

            lock.writeLock().unlock();
        }
    }

    public BookDetailed update(long id, String title, String description, String author, int pages) {

        Objects.requireNonNull(title, "book title is missing");
        Objects.requireNonNull(author, "book author is missing");

        if (pages < 1) {
            throw new IllegalArgumentException(String.format("number of pages (%s) must be positive", pages));
        }

        try {

            lock.writeLock().lock();

            BookDetailed result = books.get(id);
            result.setTitle(title);
            result.setDescription(description);
            result.setAuthor(author);
            result.setPages(pages);
            books.put(id, result);
            LOGGER.info(String.format("user %s updated the book %s", userContext.getUser().getLogin(), id));
            return result;
        } finally {

            lock.writeLock().unlock();
        }
    }

    public void remove(long id) {
        try {

            lock.writeLock().lock();

            books.remove(get(id).getId());
            LOGGER.info(String.format("user %s removed the book %s", userContext.getUser().getLogin(), id));
        } finally {

            lock.writeLock().unlock();
        }
    }

    @PostConstruct
    private void onConstructed() {
        addInternal("Fahrenheit 451 ", "Fahrenheit 451 is a dystopian novel by Ray Bradbury published in 1953. It is regarded as one of his best works.", "Bradbury, R.", 345);
        addInternal("Shantaram", "Shantaram is a 2003 novel by Gregory David Roberts, in which a convicted Australian bank robber and heroin addict who escaped from Pentridge Prison flees to India.", "Roberts, G. D.", 280);
        addInternal("1984", "Nineteen Eighty-Four, often published as 1984, is a dystopian novel by English author George Orwell published in 1949.", "Orwell, J.", 380);
        addInternal("The Master And Margarita", "The Master and Margarita (Russian: Мастер и Маргарита) is a novel by Mikhail Bulgakov, written between 1928 and 1940, but unpublished in book form until 1967.", "Bulgakov, M. A.", 507);
        addInternal("Three Comrades", "Three Comrades (German: Drei Kameraden) is a novel first published in 1936 by the German author Erich Maria Remarque.", "Remarque, E. M.", 302);
        addInternal("The Picture Of Dorian Gray", "The Picture of Dorian Gray is a philosophical novel by the writer Oscar Wilde, first published complete in the July 1890 issue of Lippincott's Monthly Magazine.", "Wilde, O.", 329);
        addInternal("Dandelion Wine", "Dandelion Wine is a 1957 novel by Ray Bradbury, taking place in the summer of 1928 in the fictional town of Green Town, Illinois, based upon Bradbury's childhood home of Waukegan, Illinois", "Bradbury, R.", 298);
        addInternal("To Kill A Mockingbird", "To Kill a Mockingbird is a novel by Harper Lee published in 1960. It was immediately successful, winning the Pulitzer Prize, and has become a classic of modern American literature.", "Harper, L.", 476);
        addInternal("The Little Prince", "The Little Prince (French: Le Petit Prince), first published in 1943, is a novella, the most famous work of the French aristocrat, writer, poet, and pioneering aviator Antoine de Saint-Exupéry.", "Saint-Exupery, A.", 217);
        addInternal("The Catcher In The Rye", "The Catcher in the Rye is a 1951 novel by J. D. Salinger. A controversial novel originally published for adults, it has since become popular with adolescent readers for its themes of teenage angst and alienation.", "Salinger, J. D.", 362);
        addInternal("Flowers For Algernon", "Flowers for Algernon is a science fiction short story and subsequent novel written by Daniel Keyes. The short story, written in 1958 and first published in the April 1959.", "Keyes, D.", 398);
    }

    private void addInternal(String title, String description, String author, int pages) {

        try {

            lock.writeLock().lock();

            long id = index++;

            BookDetailed result = new BookDetailed();
            result.setId(id);
            result.setTitle(title);
            result.setDescription(description);
            result.setAuthor(author);
            result.setPages(pages);
            books.put(id, result);
        } finally {

            lock.writeLock().unlock();
        }
    }
}
