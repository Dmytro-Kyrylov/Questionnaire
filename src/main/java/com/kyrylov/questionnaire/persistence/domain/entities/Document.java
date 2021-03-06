package com.kyrylov.questionnaire.persistence.domain.entities;

import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@EqualsAndHashCode(callSuper = true, exclude = {"responseData", "content"})
@Data
@Entity
@Table(name = "document")
public class Document extends IndexedEntity {

    private static final long serialVersionUID = -8256662615486632350L;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @OneToOne(mappedBy = "document", fetch = FetchType.LAZY)
    private ResponseData responseData;

    @Transient
    private transient byte[] content;

}
