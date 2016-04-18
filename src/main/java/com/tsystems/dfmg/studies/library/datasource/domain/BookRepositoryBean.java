package com.tsystems.dfmg.studies.library.datasource.domain;

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

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Long, BookDetailed> books = new LinkedHashMap<>();

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
            return result;
        } finally {

            lock.writeLock().unlock();
        }
    }

    public void remove(long id) {
        try {

            lock.writeLock().lock();

            books.remove(get(id).getId());
        } finally {

            lock.writeLock().unlock();
        }
    }

    @PostConstruct
    private void onConstructed() {
        add("451 По Фаренгейту", "Научно-фантастический роман-антиутопия Рэя Брэдбери, изданный в 1953 году.", "Брэдбери, Р.", 345);
        add("Шантарам", "Роман австралийского писателя Грегори Дэвида Робертса. Основой для книги послужили события собственной жизни автора. Основное действие романа разворачивается в Индии, в Бомбее (Мумбаи) в 1980-х годах.", "Робертс, Г. Д.", 280);
        add("1984", "Роман-антиутопия Джорджа Оруэлла, изданный в 1949 году.", "Оруэлл, Д.", 380);
        add("Мастер И Маргарита", "Роман Михаила Афанасьевича Булгакова, работа над которым началась в конце 1920-х годов и продолжалась вплоть до смерти писателя. Роман относится к незавершённым произведениям.", "Булгаков, М. А.", 507);
        add("Три Товарища", "Роман Эриха Мария Ремарка, работу над которым он начал в 1932 году. Роман был закончен и опубликован в датском издательстве Gyldendal под названием Kammerater в 1936 году.", "Ремарк, Э. М.", 302);
        add("Портрет Дориана Грея", "Произведение мировой литературы, единственный опубликованный роман Оскара Уайльда.", "Уайлд, Д.", 329);
        add("Вино Из Одуванчиков", "Вино из одуванчиков - произведение, выделяющееся среди литературного творчества Рэя Брэдбери личными переживаниями писателя. Это во многом автобиографическая книга, действие которой происходит летом 1928 года в вымышленном городе Грин Таун, штат Иллинойс.", "Бредбери, Р.", 298);
        add("Убить пересмешника", "Роман американской писательницы Харпер Ли, написанный в жанре воспитательного романа. Опубликован в 1960 году. В 1961 году получил Пулитцеровскую премию.", "Харпер, Л.", 476);
        add("Маленький Принц", "Аллегорическая повесть, наиболее известное произведение Антуана де Сент-Экзюпери. Впервые опубликована 6 апреля 1943 года в Нью-Йорке.", "Сент-Экзюпери, А.", 217);
        add("Над Пропастью Во Ржи", "Роман американского писателя Джерома Сэлинджера. В нём от лица 16-летнего юноши по имени Холден в весьма откровенной форме рассказывается о его обострённом восприятии американской действительности и неприятии общих канонов и морали современного общества.", "Сэлинджер, Д. Д.", 362);
        add("Цветы Для Элджернона", "Научно-фантастический рассказ Дэниела Киза («мягкая» научная фантастика).", "Киз, Д.", 398);
    }
}
