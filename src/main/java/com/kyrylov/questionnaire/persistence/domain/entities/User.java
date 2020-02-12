package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(exclude = {"responses", "roles"})
@javax.persistence.Entity
@Table(name = "app_user")
public class User extends IndexedEntity {

    private static final long serialVersionUID = 4840028749242007410L;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "activation_key")
    private String activationKey;

    @OneToMany(mappedBy = "user")
    private List<Response> responses = new LinkedList<>();

    @ManyToMany
    @JoinTable(name = "app_user_role", joinColumns = {
            @JoinColumn(name = "user_id", table = "app_user")
    }, inverseJoinColumns = {
            @JoinColumn(name = "role_id", table = "user_role")
    })
    private Set<UserRole> roles = new HashSet<>();

}
