package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true, exclude = "responseDataList")
@Data
@javax.persistence.Entity
@Table(name = "response")
@ToString(exclude = "responseDataList")
public class Response extends IndexedEntity {

    private static final long serialVersionUID = 5017820422310490908L;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "create_date")
    private Date date;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL)
    private List<ResponseData> responseDataList = new LinkedList<>();

}
