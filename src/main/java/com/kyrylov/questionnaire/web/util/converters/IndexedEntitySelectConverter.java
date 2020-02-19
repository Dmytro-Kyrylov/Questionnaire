package com.kyrylov.questionnaire.web.util.converters;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import com.kyrylov.questionnaire.web.util.models.IndexedEntitySelectItem;
import lombok.Getter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converter for {@link IndexedEntitySelectItem}
 *
 * @param <T> entity class type
 * @author Dmitrii
 */
@Getter
@FacesConverter("IndexedEntityConverter")
public class IndexedEntitySelectConverter<T extends IndexedEntity> implements Converter {

    private Collection<IndexedEntitySelectItem<T>> entities;

    private Class<T> entityClass;

    private IndexedEntitySelectConverter() {
    }

    public IndexedEntitySelectConverter(Class<T> entityClass, Function<T, String> functionForLabel) {
        this(entityClass, null, functionForLabel);
    }

    public IndexedEntitySelectConverter(Class<T> entityClass, Collection<T> entities, Function<T, String> functionForLabel) {
        if (entities != null) {
            this.entities = entities.stream()
                    .map(e -> new IndexedEntitySelectItem<T>(e, e.getId(), functionForLabel.apply(e)))
                    .collect(Collectors.toList());
        }
        this.entityClass = entityClass;
    }

    @Override
    public IndexedEntitySelectItem<T> getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        if (s != null && !s.trim().isEmpty()) {
            if (getEntities() != null && getEntities().size() != 0) {
                return getEntities().stream().filter(e -> e.getEntityId().equals(Long.parseLong(s))).findFirst().orElse(null);
            } else {
                try {
                    return new IndexedEntitySelectItem<>(DaoManager.get(getEntityClass(), Long.parseLong(s)));
                } catch (Exception e) {
                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid theme."));
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o instanceof IndexedEntitySelectItem) {
            return String.valueOf(((IndexedEntitySelectItem) o).getEntityId());
        } else {
            return null;
        }
    }
}
