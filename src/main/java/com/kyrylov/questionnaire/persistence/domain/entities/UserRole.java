package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "user_role")
public class UserRole extends IndexedEntity {

    private static final long serialVersionUID = 7930005680427934567L;

    public enum RoleEnum {
        ROLE_ADMIN, ROLE_USER;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", unique = true)
    private RoleEnum role;

    @ManyToMany(mappedBy = "roles")
    private List<User> usersWithRole = new LinkedList<>();

    public static UserRole getRoleByEnum(UserRole.RoleEnum roleEnum) throws DatabaseException {
        try {
            List<UserRole> roles = DaoManager.select(UserRole.class).where().equal(UserRole_.ROLE, roleEnum).execute();

            if (roles.size() == 0) {
                UserRole role = new UserRole();
                role.setRole(roleEnum);
                DaoManager.save(role, true);
                return role;
            } else {
                return roles.get(0);
            }
        } catch (DatabaseException e) {
            throw new DatabaseException("Cannot get role", e);
        }
    }

}
