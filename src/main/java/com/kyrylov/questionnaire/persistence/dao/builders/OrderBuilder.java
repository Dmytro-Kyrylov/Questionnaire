package com.kyrylov.questionnaire.persistence.dao.builders;

import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Builder allow to add orders to query
 *
 * @param <T> entity class type
 * @param <L> result class type
 * @author Dmitrii
 */
public class OrderBuilder<T extends IEntity, L extends Serializable> extends QueryBuilder<T, L> {

    OrderBuilder(QueryBuilder<T, L> queryBuilder) {
        super(queryBuilder);
        this.setOrders(new LinkedList<>());
    }

    public OrderBuilder<T, L> asc(String attribute) {
        return asc(attribute, getEntityClass().getSimpleName(), true);
    }

    public OrderBuilder<T, L> asc(String attribute, boolean conditionToUse) {
        return asc(attribute, getEntityClass().getSimpleName(), conditionToUse);
    }

    public OrderBuilder<T, L> asc(String attribute, String tableAlias) {
        return asc(attribute, tableAlias, true);
    }

    public OrderBuilder<T, L> asc(String attribute, String tableAlias, boolean conditionToUse) {
        if (conditionToUse) {
            this.getOrders().add(this.getCb().asc(this.getFromMap().get(tableAlias).get(attribute)));
        }
        return this;
    }

    public OrderBuilder<T, L> desc(String attribute) {
        return desc(attribute, getEntityClass().getSimpleName(), true);
    }

    public OrderBuilder<T, L> desc(String attribute, boolean conditionToUse) {
        return desc(attribute, getEntityClass().getSimpleName(), conditionToUse);
    }

    public OrderBuilder<T, L> desc(String attribute, String tableAlias) {
        return desc(attribute, tableAlias, true);
    }

    public OrderBuilder<T, L> desc(String attribute, String tableAlias, boolean conditionToUse) {
        if (conditionToUse) {
            this.getOrders().add(this.getCb().desc(this.getFromMap().get(tableAlias).get(attribute)));
        }
        return this;
    }

}
