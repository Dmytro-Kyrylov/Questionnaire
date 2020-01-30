package com.kyrylov.questionnaire.web.security;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = null;
        try {
            List<User> users = DaoManager.select(User.class).where().equal(User_.EMAIL, s).execute();
            if (users != null && users.size() != 0) {
                user = users.get(0);
            }
        } catch (DatabaseException e) {
            throw new UsernameNotFoundException("Error when trying to load user", e);
        }
        if (user == null) {
            throw new UsernameNotFoundException("User was not found");
        }

        return new UserDetailsImpl(user);
    }

}
