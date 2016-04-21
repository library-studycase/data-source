package com.tsystems.dfmg.studies.library.datasource.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class UserBisServiceBean implements UserBisService {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Long, User> users = new LinkedHashMap<>();

    @Autowired
    UserRepository userRepository;

    @Override
    public UserBis get(String login, String password) {
        try {
            lock.readLock().lock();

            User User = userRepository.get(login);
            if (User == null) {
                throw new IllegalArgumentException(String.format("no user with such login (%s) was found", login));
            }
            if (!User.getPassword().equals(password)) {
                throw new IllegalArgumentException("invalid password");
            }
            UserBis userBis = new UserBis();
            userBis.setNativeId(User.getId());
            userBis.setLogin(User.getLogin());
            userBis.roles = User.roles;
            userBis.setCreated(User.getCreated());
            userBis.setLastModified(User.getLastModified());
            return userBis;
        } finally {
            lock.readLock().unlock();
        }
    }

}
