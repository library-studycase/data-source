package com.tsystems.dfmg.studies.library.datasource.domain;

public interface BookRepository {

    BookDetailed get(long id);

    BookListPage getPage(int offset, int limit, String titleFilter);

    BookDetailed add(String title, String description, String author, int pages);

    BookDetailed update(long id, String title, String description, String author, int pages);

    void remove(long id);
}
