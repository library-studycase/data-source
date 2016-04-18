package com.tsystems.dfmg.studies.library.datasource.domain;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class BookReducerBean implements Function<BookDetailed, Book> {

    @Override
    public Book apply(final BookDetailed detailed) {
        final Book result = new Book();
        result.setId(detailed.getId());
        result.setTitle(detailed.getTitle());
        result.setAuthor(detailed.getAuthor());
        result.setPages(detailed.getPages());
        return result;
    }
}
