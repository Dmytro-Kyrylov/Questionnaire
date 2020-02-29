package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "user_role")
@Table(name = "user_role")
public class UserRole extends IndexedEntity {

    private static final long serialVersionUID = 7930005680427934567L;

    public enum RoleEnum {
        ROLE_ADMIN, ROLE_USER
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", unique = true)
    private RoleEnum role;

}
