package com.tsystems.dfmg.studies.library.datasource.domain;

import java.util.List;

public interface UserRepository {

    User get(long id);

    User get(String login);

    User add(String login, String password, List<String> roles);

    User update(long id, String login, String password, List<String> roles);

    void remove(long id);

}
