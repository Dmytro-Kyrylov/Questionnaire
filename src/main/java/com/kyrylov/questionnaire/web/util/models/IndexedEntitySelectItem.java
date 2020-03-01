package com.kyrylov.questionnaire.web.util.models;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexedEntitySelectItem<T extends IndexedEntity> {

    private T entity;

    private Long entityId;

    private String label;

    public IndexedEntitySelectItem(T entity) {
        this(entity, entity.getId(), entity.toString());
    }

}
