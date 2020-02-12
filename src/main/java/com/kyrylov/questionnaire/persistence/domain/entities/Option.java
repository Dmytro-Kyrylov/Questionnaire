package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;

@EqualsAndHashCode(callSuper = true, exclude = "responseDataList")
@Data
@javax.persistence.Entity
@Table(name = "option")
@ToString(exclude = "responseDataList")
public class Option extends IndexedEntity {

    private static final long serialVersionUID = 7699421533554590043L;

    @Column(name = "text")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private Field field;

    @ManyToMany(mappedBy = "selectedOptions", fetch = FetchType.LAZY)
    private List<ResponseData> responseDataList;
}
