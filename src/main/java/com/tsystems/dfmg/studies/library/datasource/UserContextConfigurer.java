package com.tsystems.dfmg.studies.library.datasource;

import com.tsystems.dfmg.studies.library.datasource.domain.User;

public interface UserContextConfigurer extends UserContext {

    void setUser(User user);
}
