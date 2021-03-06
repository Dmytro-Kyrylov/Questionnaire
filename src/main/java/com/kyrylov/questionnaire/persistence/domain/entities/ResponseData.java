package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "response_data")
public class ResponseData implements IEntity {

    private static final long serialVersionUID = -4429962591671973629L;

    public ResponseData(Field field, Response response) {
        setField(field);
        setResponse(response);
    }

    @EmbeddedId
    private ResponseDataIdentifier identifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fieldId")
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("responseId")
    private Response response;

    @Column(name = "text")
    private String text;

    @Column(name = "big_text", columnDefinition = "TEXT")
    private String bigText;

    @Column(name = "date_data")
    private LocalDate date;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "document_id")
    private Document document;

    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "response_data_option", joinColumns = {
            @JoinColumn(name = "response_data_field_id", referencedColumnName = "field_id"),
            @JoinColumn(name = "response_data_response_id", referencedColumnName = "response_id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "option_id", table = "option")
    })
    private Set<Option> selectedOptions = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void initResponseData() {
        setIdentifier(new ResponseDataIdentifier(getField().getId(), getResponse().getId()));
    }

    public boolean isEmpty() {
        return (getText() == null || getText().isEmpty()) && (getBigText() == null || getBigText().isEmpty())
                && getDate() == null && getDocument() == null
                && (getSelectedOptions() == null || getSelectedOptions().size() == 0);
    }

    public Optional<String> getDataAccordingTypeAsString() {
        switch (this.getField().getType()) {
            case SINGLE_LINE_TEXT:
                return Optional.ofNullable(this.getText());
            case MULTILINE_TEXT:
                return Optional.ofNullable(this.getBigText());
            case RADIO_BUTTON:
            case CHECKBOX:
            case COMBOBOX:
                return Optional.of(this.getSelectedOptions().stream().map(Option::getText)
                        .collect(Collectors.joining(";"))).filter(x->!x.isEmpty());
            case DATE:
                return Optional.ofNullable(this.getDate()).map(LocalDate::toString);
            case FILE:
                return Optional.ofNullable(this.getDocument()).map(Document::getFileName);
            default:
                return Optional.empty();
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @EqualsAndHashCode
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Embeddable
    static class ResponseDataIdentifier implements Serializable {

        private static final long serialVersionUID = 649062580411926903L;

        @SuppressWarnings("JpaDataSourceORMInspection")
        @Column(name = "field_id")
        private Long fieldId;

        @SuppressWarnings("JpaDataSourceORMInspection")
        @Column(name = "response_id")
        private Long responseId;
    }

}
