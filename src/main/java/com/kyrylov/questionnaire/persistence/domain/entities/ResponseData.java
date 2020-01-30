package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "response_data")
public class ResponseData implements IEntity {

    private static final long serialVersionUID = -4429962591671973629L;

    @Data
    @Embeddable
    static class ResponseDataIdentifier implements Serializable {

        private static final long serialVersionUID = 649062580411926903L;

        @Column(name = "field_id")
        private Long fieldId;

        @Column(name = "response_id")
        private Long responseId;

        public ResponseDataIdentifier() {
        }

        private ResponseDataIdentifier(Long fieldId, Long responseId) {
            this.fieldId = fieldId;
            this.responseId = responseId;
        }
    }

    public ResponseData() {
    }

    public ResponseData(Field field, Response response) {
        initResponseData(field, response);
    }

    @EmbeddedId
    private ResponseDataIdentifier identifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fieldId")
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("responseId")
    private Response response;

    @Column(name = "data")
    private String data;

    @Column(name = "date_data")
    private Date date;

    @OneToOne(cascade = CascadeType.ALL)
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
    private List<Option> selectedOptions = new ArrayList<>();

    public void initResponseData(Field field, Response response) {
        setIdentifier(new ResponseDataIdentifier(field.getId(), response.getId()));
        setField(field);
        setResponse(response);
    }

    public boolean isEmpty() {
        return (getData() == null || getData().isEmpty()) && getDate() == null && getDocument() == null
                && (getSelectedOptions() == null || getSelectedOptions().size() == 0);
    }

    public String getDataAccordingTypeAsString() {
        switch (this.getField().getType()) {
            case SINGLE_LINE_TEXT:
            case MULTILINE_TEXT:
                return this.getData();
            case RADIO_BUTTON:
            case CHECKBOX:
            case COMBOBOX:
                return this.getSelectedOptions().stream().map(Option::getText).collect(Collectors.joining(";"));
            case DATE:
                return this.getDate().toString();
            case FILE:
                return this.getDocument().getFileName();
            default:
                return "";
        }
    }

}
