package com.kyrylov.questionnaire.persistence.dao.builders;

import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import org.hibernate.Session;

import javax.persistence.criteria.*;
import java.io.Serializable;

/**
 * First builder that created for configuring query. Allow to add joins
 * <p>
 * All joins stored in {@link QueryBuilder#getFromMap()} map. Main entity also has own alias - it is a simpleName of class
 *
 * @param <T> entity class type
 * @param <L> result class type
 * @author Dmitrii
 */
public class JoinBuilder<T extends IEntity, L extends Serializable> extends QueryBuilder<T, L> {

    public JoinBuilder(Class<T> tClass, Session session, CriteriaBuilder criteriaBuilder, CriteriaQuery<L> criteria, Root<T> root) {
        super(tClass, session, criteriaBuilder, criteria, root);
    }

    public JoinBuilder<T, L> innerJoin(String tableAttribute, String alias) {
        return join(this.getRoot(), tableAttribute, alias, JoinType.INNER, true);
    }

    public JoinBuilder<T, L> innerJoin(String tableAttribute, String alias, boolean conditionToUse) {
        return join(this.getRoot(), tableAttribute, alias, JoinType.INNER, conditionToUse);
    }

    public JoinBuilder<T, L> innerJoin(String mainTable, String tableAttribute, String alias) {
        return join(getFromMap().get(mainTable), tableAttribute, alias, JoinType.INNER, true);
    }

    public JoinBuilder<T, L> innerJoin(String mainTable, String tableAttribute, String alias, boolean conditionToUse) {
        return join(getFromMap().get(mainTable), tableAttribute, alias, JoinType.INNER, conditionToUse);
    }

    public JoinBuilder<T, L> leftJoin(String tableAttribute, String alias) {
        return join(this.getRoot(), tableAttribute, alias, JoinType.LEFT, true);
    }

    public JoinBuilder<T, L> leftJoin(String tableAttribute, String alias, boolean conditionToUse) {
        return join(this.getRoot(), tableAttribute, alias, JoinType.LEFT, conditionToUse);
    }

    public JoinBuilder<T, L> leftJoin(String mainTable, String tableAttribute, String alias) {
        return join(getFromMap().get(mainTable), tableAttribute, alias, JoinType.LEFT, true);
    }

    public JoinBuilder<T, L> leftJoin(String mainTable, String tableAttribute, String alias, boolean conditionToUse) {
        return join(getFromMap().get(mainTable), tableAttribute, alias, JoinType.LEFT, conditionToUse);
    }

    private <X extends IEntity> JoinBuilder<T, L> join(From<X, ? extends IEntity> from, String tableAttribute,
                                                       String alias, JoinType joinType, boolean conditionToUse) {
        if (conditionToUse) {
            Join<X, ? extends IEntity> join = from.join(tableAttribute, joinType);
            join.alias(alias);
            getFromMap().put(alias, join);
        }
        return this;
    }

    public ConditionBuilder<T, L> where() {
        return new ConditionBuilder<>(this);
    }

    public OrderBuilder<T, L> orderBy() {
        return new OrderBuilder<>(this);
    }

}
