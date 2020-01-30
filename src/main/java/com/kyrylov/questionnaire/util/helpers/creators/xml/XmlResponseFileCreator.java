package com.kyrylov.questionnaire.util.helpers.creators.xml;

import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.domain.entities.Response;
import com.kyrylov.questionnaire.persistence.domain.entities.ResponseData;
import lombok.AccessLevel;
import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Collection;
import java.util.Locale;

@Getter(AccessLevel.PRIVATE)
public class XmlResponseFileCreator extends XmlFileCreator {

    private Collection<Response> responses;

    public XmlResponseFileCreator(Locale locale, Collection<Response> responses) {
        super(locale);
        this.responses = responses;
    }

    @Override
    public byte[] createFile() throws Exception {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        Document document = documentBuilder.newDocument();

        Element root = document.createElement(resource("fileXmlResponseRoot"));
        document.appendChild(root);

        for (Response response : getResponses()) {
            Element responseNode = document.createElement(resource("fileXmlResponseNode"));

            responseNode.setAttribute(resource("fileXmlResponseUser"),
                    response.getUser() != null ? response.getUser().getEmail() : "");
            responseNode.setAttribute(resource("fileXmlResponseCreateDate"), response.getDate().toString());

            for (ResponseData responseData : response.getResponseDataList()) {
                Field field = responseData.getField();
                Element fieldNode = document.createElement(resource("fileXmlResponseFieldNode"));

                fieldNode.setAttribute(resource("fileXmlResponseFieldLabel"), field.getLabel());

                Element fieldTypeNode = document.createElement(field.getType().name().toLowerCase());

                if (field.getType().isMultiOptionsType()) {
                    Element optionRoot = document.createElement(resource("fileXmlResponseOptionRoot"));

                    for (Option option : responseData.getSelectedOptions()) {
                        Element optionNode = document.createElement(resource("fileXmlResponseOptionNode"));
                        optionNode.appendChild(document.createTextNode(option.getText()));
                        optionRoot.appendChild(optionNode);
                    }
                    fieldTypeNode.appendChild(optionRoot);
                } else {
                    fieldTypeNode.appendChild(document.createTextNode(responseData.getDataAccordingTypeAsString()));
                }

                fieldNode.appendChild(fieldTypeNode);
                responseNode.appendChild(fieldNode);
            }
            root.appendChild(responseNode);
        }


        return getBytes(document);
    }
}
