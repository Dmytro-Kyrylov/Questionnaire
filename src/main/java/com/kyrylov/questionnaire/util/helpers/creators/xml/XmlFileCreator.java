package com.kyrylov.questionnaire.util.helpers.creators.xml;

import com.kyrylov.questionnaire.util.helpers.creators.BaseFileCreator;
import lombok.AccessLevel;
import lombok.Getter;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Getter(AccessLevel.PRIVATE)
abstract class XmlFileCreator extends BaseFileCreator {

    public XmlFileCreator(Locale locale) {
        super(locale);
    }

    abstract byte[] createFile() throws Exception;

    protected byte[] getBytes(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new ByteArrayOutputStream());

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(domSource, streamResult);

        return ((ByteArrayOutputStream) streamResult.getOutputStream()).toByteArray();
    }

}
