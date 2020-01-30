package com.kyrylov.questionnaire.util.helpers.creators.xml;

import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import lombok.AccessLevel;
import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Collection;
import java.util.Locale;

@Getter(AccessLevel.PRIVATE)
public class XmlFieldFileCreator extends XmlFileCreator {

    private Collection<Field> fields;

    public XmlFieldFileCreator(Locale locale, Collection<Field> fields) {
        super(locale);
        this.fields = fields;
    }

    @Override
    public byte[] createFile() throws Exception {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        Document document = documentBuilder.newDocument();

        Element root = document.createElement(resource("fileXmlFieldRoot"));
        document.appendChild(root);

        for (Field field : getFields()) {
            Element fieldNode = document.createElement(resource("fileXmlFieldNode"));

            fieldNode.setAttribute(resource("fileXmlFieldIsRequired"), String.valueOf(field.getRequired()));
            fieldNode.setAttribute(resource("fileXmlFieldIsActive"), String.valueOf(field.getActive()));

            Element label = document.createElement(resource("fileXmlFieldLabel"));
            label.appendChild(document.createTextNode(field.getLabel()));
            fieldNode.appendChild(label);

            Element type = document.createElement(resource("fileXmlFieldType"));
            type.appendChild(document.createTextNode(field.getType().name()));
            fieldNode.appendChild(type);

            if (field.getType().isOptionsType()) {
                Element options = document.createElement(resource("fileXmlFieldOptions"));
                for (Option option : field.getOptions()) {
                    Element optionElement = document.createElement(resource("fileXmlFieldOption"));
                    optionElement.appendChild(document.createTextNode(option.getText()));
                    options.appendChild(optionElement);
                }
                fieldNode.appendChild(options);
            }

            root.appendChild(fieldNode);
        }

        return getBytes(document);
    }
}
