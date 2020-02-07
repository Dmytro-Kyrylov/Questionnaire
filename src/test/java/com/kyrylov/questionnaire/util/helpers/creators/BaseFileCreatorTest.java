package com.kyrylov.questionnaire.util.helpers.creators;

import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.domain.entities.Response;
import com.kyrylov.questionnaire.persistence.domain.entities.ResponseData;
import com.kyrylov.questionnaire.util.helpers.creators.xls.XlsFieldFileCreator;
import com.kyrylov.questionnaire.util.helpers.creators.xls.XlsResponseFileCreator;
import com.kyrylov.questionnaire.util.helpers.creators.xml.XmlFieldFileCreator;
import com.kyrylov.questionnaire.util.helpers.creators.xml.XmlResponseFileCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

class BaseFileCreatorTest {

    private Collection<Field> fields;
    private Collection<Response> responses;

    @BeforeEach
    void setUp() {
        Field field = new Field();
        field.setLabel("test");
        field.setActive(true);
        field.setType(Field.FieldType.CHECKBOX);
        Option option = new Option();
        option.setText("test");
        field.setOptions(Collections.singletonList(option));

        Response response = new Response();
        response.setDate(new Date());
        ResponseData responseData = new ResponseData();
        responseData.setSelectedOptions(field.getOptions());
        responseData.setField(field);
        response.setResponseDataList(Collections.singletonList(responseData));

        this.fields = Collections.singleton(field);
        this.responses = Collections.singleton(response);
    }

    @Test
    void testXlsFields() throws Exception {
        byte[] xls = new XlsFieldFileCreator(Locale.ENGLISH, this.fields).createXls();
        Assertions.assertNotNull(xls);
    }

    @Test
    void testXlsResponses() throws Exception {
        byte[] xlsx = new XlsResponseFileCreator(Locale.ENGLISH, this.responses, this.fields).createXlsx();
        Assertions.assertNotNull(xlsx);
    }

    @Test
    void testXmlFields() throws Exception {
        byte[] xml = new XmlFieldFileCreator(Locale.ENGLISH, this.fields).createFile();
        Assertions.assertNotNull(xml);
    }

    @Test
    void testXmlResponses() throws Exception {
        byte[] file = new XmlResponseFileCreator(Locale.ENGLISH, this.responses).createFile();
        Assertions.assertNotNull(file);
    }
}