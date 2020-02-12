package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true, exclude = {"responseDataList", "options"})
@Data
@javax.persistence.Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "field")
@ToString(exclude = {"responseDataList", "options"})
public class Field extends IndexedEntity {

    private static final long serialVersionUID = -9016927841870119279L;

    //enums.properties
    public enum FieldType {
        SINGLE_LINE_TEXT,
        MULTILINE_TEXT,
        RADIO_BUTTON,
        CHECKBOX,
        COMBOBOX,
        DATE,
        FILE;

        public boolean isOptionsType() {
            return this.isMultiOptionsType() || this.isSingleOptionType();
        }

        public boolean isMultiOptionsType() {
            return this.equals(FieldType.CHECKBOX);
        }

        public boolean isSingleOptionType() {
            return this.equals(FieldType.RADIO_BUTTON) || this.equals(FieldType.COMBOBOX);
        }

    }

    @Column(name = "label")
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private FieldType type;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "active")
    private Boolean active;

    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL)
    private List<Option> options = new LinkedList<>();

    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "field", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ResponseData> responseDataList = new ArrayList<>();

}
