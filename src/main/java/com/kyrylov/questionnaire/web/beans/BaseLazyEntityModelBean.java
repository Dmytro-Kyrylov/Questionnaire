package com.kyrylov.questionnaire.web.beans;

import com.kyrylov.questionnaire.persistence.dao.builders.ConditionBuilder;
import com.kyrylov.questionnaire.persistence.dao.builders.JoinBuilder;
import com.kyrylov.questionnaire.persistence.dao.builders.OrderBuilder;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import com.kyrylov.questionnaire.web.util.models.EntityLazyListModel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.LazyDataModel;

import java.util.function.Function;

/**
 * Page with lazy model
 *
 * @param <T> entity class type
 * @author Dmitrii
 */
@Getter
@Setter
public abstract class BaseLazyEntityModelBean<T extends IndexedEntity> extends BasePageBean {

    private static final long serialVersionUID = -3056314505467829085L;

    private LazyDataModel<T> lazyModel;

    protected void loadList(Class<T> clazz, Function<JoinBuilder<T, ?>, JoinBuilder<T, ?>> join,
                            Function<ConditionBuilder<T, ?>, ConditionBuilder<T, ?>> restrictions,
                            Function<OrderBuilder<T, ?>, OrderBuilder<T, ?>> orders) {
        this.setLazyModel(new EntityLazyListModel<T>(clazz, join, restrictions, orders));
    }

}
