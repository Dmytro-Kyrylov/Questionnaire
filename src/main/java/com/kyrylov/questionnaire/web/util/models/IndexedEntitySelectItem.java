package com.kyrylov.questionnaire.web.util.models;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.Data;

@Data
public class IndexedEntitySelectItem<T extends IndexedEntity> {

    private T entity;

    private Long entityId;

    private String label;

    public IndexedEntitySelectItem(T entity, Long entityId, String label) {
        this.entity = entity;
        this.entityId = entityId;
        this.label = label;
    }

    public IndexedEntitySelectItem(T entity) {
        this(entity, entity.getId(), entity.toString());
    }

}
