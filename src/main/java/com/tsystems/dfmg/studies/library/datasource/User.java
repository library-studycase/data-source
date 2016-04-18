package com.tsystems.dfmg.studies.library.datasource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class User {

    private final String login;
    private final Set<String> roles;

    public User(String login, String... roles) {
        this.login = login;
        this.roles = new HashSet<>(Arrays.asList(roles));
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
