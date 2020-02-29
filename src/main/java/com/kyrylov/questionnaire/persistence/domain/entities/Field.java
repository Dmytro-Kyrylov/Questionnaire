package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@EqualsAndHashCode(callSuper = true, of = {"label", "typeId", "required", "active"})
@Data
@javax.persistence.Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "field")
@ToString(of = {"label", "type", "required", "active"})
public class Field extends IndexedEntity {

    private static final long serialVersionUID = -9016927841870119279L;

    @Column(name = "label")
    private String label;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Column(name = "type")
    private Long typeId;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "active")
    private Boolean active;

    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Option> options = new HashSet<>();

    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "field", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ResponseData> responseDataList = new ArrayList<>();

    @Transient
    private FieldType type;

    @PreUpdate
    @PrePersist
    private void setTypeIdByEnum() {
        setTypeId(getType() != null ? getType().getId() : null);
    }

    @PostLoad
    private void setTypeEnumById() {
        setType(FieldType.findTypeById(getTypeId()).orElse(null));
    }

    //enums.properties
    @Getter
    @AllArgsConstructor
    public enum FieldType {
        SINGLE_LINE_TEXT(1L),
        MULTILINE_TEXT(2L),
        RADIO_BUTTON(3L),
        CHECKBOX(4L),
        COMBOBOX(5L),
        DATE(6L),
        FILE(7L);

        private Long id;

        public boolean isOptionsType() {
            return this.isMultiOptionsType() || this.isSingleOptionType();
        }

        public boolean isMultiOptionsType() {
            return this.equals(FieldType.CHECKBOX);
        }

        public boolean isSingleOptionType() {
            return this.equals(FieldType.RADIO_BUTTON) || this.equals(FieldType.COMBOBOX);
        }

        public static Optional<FieldType> findTypeById(Long id) {
            return Arrays.stream(values()).filter(fieldType -> fieldType.getId().equals(id)).findFirst();
        }
    }

}
