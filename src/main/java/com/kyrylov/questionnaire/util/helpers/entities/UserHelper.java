package com.kyrylov.questionnaire.util.helpers.entities;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole_;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;

/**
 * Contains business methods to work with User entity
 * {@link User}
 *
 * @author Dmitrii
 */
public class UserHelper {

    /**
     * Allows you to find out if a passed email has been registered on any user
     *
     * @param email user`s email address
     * @return flag that indicate if this email is exist in database
     * @throws DatabaseException if any exception with DB occurs
     */
    public static boolean isUserEmailAlreadyExistInDB(String email) throws DatabaseException {
        long usersWithEnteredEmail = DaoManager.getCount(User.class).where()
                .equal(User_.EMAIL, email).singleResult();
        return usersWithEnteredEmail != 0;
    }

    /**
     * Allows you to get the UserRole entity from the database or creates a new instance with the current enum, saves it in the database and returns
     *
     * @param roleEnum enum of the role {@link com.kyrylov.questionnaire.persistence.domain.entities.UserRole.RoleEnum}
     * @return UserRole entity {@link UserRole}
     * @throws DatabaseException if any exception with DB occurs
     */
    public static UserRole getRoleByEnum(UserRole.RoleEnum roleEnum) throws DatabaseException {
        try {
            UserRole userRole = DaoManager.getByField(UserRole.class, UserRole_.ROLE, roleEnum);

            if (userRole == null) {
                userRole = new UserRole();
                userRole.setRole(roleEnum);
                DaoManager.save(userRole, true);
            }
            return userRole;
        } catch (DatabaseException e) {
            throw new DatabaseException("Cannot get role", e);
        }
    }
}
