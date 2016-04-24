package com.tsystems.dfmg.studies.library.datasource.domain;

import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class UserRepositoryBean implements UserRepository {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Long, User> usersIds = new LinkedHashMap<>();
    private final Map<String, User> usersLogins = new LinkedHashMap<>();
    private long index = 1;

    @Override
    public User get(long id) {
        try {
            lock.readLock().lock();

            User result = usersIds.get(id);
            if (result == null) {
                throw new IllegalArgumentException(String.format("no user with such id (%s) was found", id));
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public User get(String login) {
        try {
            lock.readLock().lock();

            User result = usersLogins.get(login);
            if (result == null) {
                throw new IllegalArgumentException(String.format("no user with such login (%s) was found", login));
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public User add(String login, String password, List<String> roles) {
        Objects.requireNonNull(login, "user login is missing");
        Objects.requireNonNull(password, "user password is missing");
        Objects.requireNonNull(roles, "user roles is missing");

        try {
            lock.writeLock().lock();

            long id = index++;

            User result = new User();
            result.setId(id);
            result.setLogin(login);
            result.setPassword(password);
            result.getRoles().addAll(roles);
            result.setCreated(getXMLGregorianCalendarNow());
            result.setLastModified(getXMLGregorianCalendarNow());
            usersIds.put(id, result);
            usersLogins.put(login, result);
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public User update(long id, String login, String password, List<String> roles) {
        Objects.requireNonNull(id, "user id is missing");
        Objects.requireNonNull(login, "user login is missing");
        Objects.requireNonNull(password, "user password is missing");
        Objects.requireNonNull(roles, "user roles is missing");

        try {
            lock.writeLock().lock();

            User result = usersIds.get(id);
            result.setLogin(login);
            result.setPassword(password);
            result.roles = roles;
            result.setLastModified(getXMLGregorianCalendarNow());
            usersIds.put(id, result);
            usersLogins.put(login, result);
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(long id) {
        try {
            lock.writeLock().lock();

            User User = get(id);
            usersIds.remove(User.getId());
            usersLogins.remove(User.getLogin());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @PostConstruct
    private void onConstructed() {
        add(
                "admin",
                "admin",
                new ArrayList<String>() {{
                    add("READER");
                    add("ADDER");
                    add("MODIFIER");
                }}
        );
        add(
                "user",
                "user",
                new ArrayList<String>() {{
                    add("READER");
                    add("ADDER");
                }}
        );
    }

    public XMLGregorianCalendar getXMLGregorianCalendarNow() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException("Can not create new XMLGregorianCalendar");
        }
        return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }

}
