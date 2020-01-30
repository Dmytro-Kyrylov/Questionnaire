package com.kyrylov.questionnaire.persistence.domain;

import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@MappedSuperclass
public class IndexedEntity implements IEntity {

    private static final long serialVersionUID = -450077756218173034L;

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public boolean isNew() {
        return id == null || id.equals(0L);
    }

}
