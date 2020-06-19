package com.kyrylov.questionnaire.web.security;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionalUser;
        try {
            optionalUser = DaoManager.getByField(User.class, User_.EMAIL, email);
        } catch (DatabaseException e) {
            throw new UsernameNotFoundException("Error when trying to load user", e);
        }

        return new UserDetailsImpl(optionalUser.orElseThrow(() -> new UsernameNotFoundException("User was not found")));
    }

}
