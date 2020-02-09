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
        ROLE_ADMIN, ROLE_USER
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", unique = true)
    private RoleEnum role;

    @ManyToMany(mappedBy = "roles")
    private List<User> usersWithRole = new LinkedList<>();

}
