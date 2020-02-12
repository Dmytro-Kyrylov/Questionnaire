package com.kyrylov.questionnaire.util.helpers.entities;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserHelperTest {

    @Test
    void getRoleByEnum() throws DatabaseException {
        UserRole roleByEnum = UserHelper.getRoleByEnum(UserRole.RoleEnum.ROLE_ADMIN);
        Assertions.assertNotNull(roleByEnum, "Role was not created");

        DaoManager.getSession().evict(roleByEnum);
        Assertions.assertFalse(DaoManager.getSession().contains(roleByEnum), "Role still in session");

        UserRole roleByEnum1 = UserHelper.getRoleByEnum(UserRole.RoleEnum.ROLE_ADMIN);
        Assertions.assertNotNull(roleByEnum1, "Role was not found");

        Assertions.assertEquals(roleByEnum.getId(), roleByEnum1.getId(), "Roles are different");
    }
}