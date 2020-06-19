package com.kyrylov.questionnaire.persistence.dao.builders;

import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Allow to add conditions((restrictions) to query.
 * Final result stored in {@link QueryBuilder#getPredicate()} field. This field must be set manually when completing this builder.
 * <p>
 * To use brackets use {@link ConditionBuilder#openBracket()} and {@link ConditionBuilder#closeBracket()}
 * Every time when bracket are opened it create a new "level" in predicates`s stack{@link ConditionBuilder#predicates} that will contained restrictions in brackets.
 * <p>
 * At {@link ConditionBuilder#predicates} stored pairs with current operator{@link Operator}(default is -AND) and predicate
 *
 * @param <T> entity class type
 * @param <L> result class type
 * @author Dmitrii
 */
@SuppressWarnings("ConstantConditions")
public class ConditionBuilder<T extends IEntity, L extends Serializable> extends QueryBuilder<T, L> {

    private Deque<Tuple> predicates;

    ConditionBuilder(QueryBuilder<T, L> queryBuilder) {
        super(queryBuilder);
        initConditionBuilder();
    }

    private void initConditionBuilder() {
        this.predicates = new ArrayDeque<>();
        openBracket();
    }

    public <R> ConditionBuilder<T, L> equal(String attribute, R value, boolean... conditionsToUse) {
        return equal(attribute, value, getEntityClass().getSimpleName(), conditionsToUse);
    }

    public <R> ConditionBuilder<T, L> equal(String attribute, R value, String tableAlias, boolean... conditionsToUse) {
        if (checkConditions(conditionsToUse)) {
            createPredicate(this.getCb().equal(this.getFromMap().get(tableAlias).<R>get(attribute), value));
        }
        return this;
    }

    public <R> ConditionBuilder<T, L> notEqual(String attribute, R value, boolean... conditionsToUse) {
        return notEqual(attribute, value, getEntityClass().getSimpleName(), conditionsToUse);
    }

    public <R> ConditionBuilder<T, L> notEqual(String attribute, R value, String tableAlias, boolean... conditionsToUse) {
        if (checkConditions(conditionsToUse)) {
            createPredicate(this.getCb().notEqual(this.getFromMap().get(tableAlias).<R>get(attribute), value));
        }
        return this;
    }

    public <X, R extends List<X>> ConditionBuilder<T, L> in(String attribute, R value, boolean... conditionsToUse) {
        return in(attribute, value, getEntityClass().getSimpleName(), conditionsToUse);
    }

    public <X, R extends List<X>> ConditionBuilder<T, L> in(String attribute, R value, String tableAlias, boolean... conditionsToUse) {
        if (checkConditions(conditionsToUse)) {
            CriteriaBuilder.In<X> in = this.getCb().in(this.getFromMap().get(tableAlias).get(attribute));
            for (X x : value) {
                in.value(x);
            }
            createPredicate(in);
        }
        return this;
    }

    public ConditionBuilder<T, L> isNull(String attribute, boolean... conditionsToUse) {
        return isNull(attribute, getEntityClass().getSimpleName(), conditionsToUse);
    }

    public ConditionBuilder<T, L> isNull(String attribute, String tableAlias, boolean... conditionsToUse) {
        if (checkConditions(conditionsToUse)) {
            createPredicate(this.getCb().isNull(this.getFromMap().get(tableAlias).get(attribute)));
        }
        return this;
    }

    public ConditionBuilder<T, L> isNotNull(String attribute, boolean... conditionsToUse) {
        return isNotNull(attribute, getEntityClass().getSimpleName(), conditionsToUse);
    }

    public ConditionBuilder<T, L> isNotNull(String attribute, String tableAlias, boolean... conditionsToUse) {
        if (checkConditions(conditionsToUse)) {
            createPredicate(this.getCb().isNotNull(this.getFromMap().get(tableAlias).get(attribute)));
        }
        return this;
    }

    public ConditionBuilder<T, L> like(String attribute, String value, boolean... conditionsToUse) {
        return like(attribute, value, getEntityClass().getSimpleName(), conditionsToUse);
    }

    public ConditionBuilder<T, L> like(String attribute, String value, String tableAlias, boolean... conditionsToUse) {
        if (checkConditions(conditionsToUse)) {
            createPredicate(this.getCb().like(this.getFromMap().get(tableAlias).get(attribute), "%" + value + "%"));
        }
        return this;
    }

    public ConditionBuilder<T, L> not() {
        predicates.peek().setNotOperator(!predicates.peek().isNotOperator());
        return this;
    }

    public ConditionBuilder<T, L> openBracket() {
        predicates.push(new Tuple(ConditionBuilder.Operator.AND, this.getCb().and()));
        return this;
    }

    /**
     * Create predicate by this level(bracket`s restrictions) and remove this level from stack
     *
     * @return this builder
     * @throws DatabaseException if was closed not existing bracket
     */
    public ConditionBuilder<T, L> closeBracket() throws DatabaseException {
        Tuple predicateTuple = predicates.pop();
        if (predicates.isEmpty()) {
            throw new DatabaseException("Closing not existing bracket");
        }
        createPredicate(predicateTuple.getPredicate());
        return this;
    }

    public ConditionBuilder<T, L> or() {
        predicates.peek().setOperator(ConditionBuilder.Operator.OR);
        return this;
    }

    public ConditionBuilder<T, L> and() {
        predicates.peek().setOperator(ConditionBuilder.Operator.AND);
        return this;
    }

    /**
     * Combines the passed predicate with the current level predicate by the corresponding operation contained in the current level
     *
     * @param predicate predicate for merging with current
     */
    private void createPredicate(Predicate predicate) {
        Tuple currentPredicateTuple = predicates.peek();
        switch (currentPredicateTuple.getOperator()) {
            case OR:
                currentPredicateTuple.setPredicate(this.getCb().or(currentPredicateTuple.getPredicate(),
                        addNotOperator(predicate, currentPredicateTuple.isNotOperator())));
                break;
            case AND:
                currentPredicateTuple.setPredicate(this.getCb().and(currentPredicateTuple.getPredicate(),
                        addNotOperator(predicate, currentPredicateTuple.isNotOperator())));
                break;
        }
        currentPredicateTuple.setNotOperator(false);
        and();
    }

    private Predicate addNotOperator(Predicate predicate, boolean notOperator) {
        if (notOperator) {
            return this.getCb().not(predicate);
        }
        return predicate;
    }

    private boolean checkConditions(boolean... conditions) {
        if (conditions != null && conditions.length > 0) {
            for (boolean condition : conditions) {
                if (!condition) return false;
            }
        }
        return true;
    }

    /**
     * Create final predicate
     *
     * @return all entered restrictions in one predicate
     * @throws DatabaseException if brackets are not closed correctly
     */
    private Predicate getResultPredicate() throws DatabaseException {
        if (this.predicates.size() != 1) {
            throw new DatabaseException("Wrong brackets");
        }
        return predicates.pop().getPredicate();
    }

    public OrderBuilder<T, L> orderBy() throws DatabaseException {
        this.setPredicate(getResultPredicate());
        return new OrderBuilder<>(this);
    }

    @Override
    public List<L> list() throws DatabaseException {
        this.setPredicate(getResultPredicate());
        return super.list();
    }

    @Override
    public Optional<L> singleResult() throws DatabaseException {
        this.setPredicate(getResultPredicate());
        return super.singleResult();
    }

    private enum Operator {
        OR, AND
    }

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static final class Tuple {
        private ConditionBuilder.Operator operator;
        private Predicate predicate;
        private boolean notOperator;

        private Tuple(ConditionBuilder.Operator operator, Predicate predicate, boolean notOperator) {
            this.operator = operator;
            this.predicate = predicate;
        }

        private Tuple(Operator operator, Predicate predicate) {
            this.operator = operator;
            this.predicate = predicate;
        }
    }
}
