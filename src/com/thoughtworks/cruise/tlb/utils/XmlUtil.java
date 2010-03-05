package com.thoughtworks.cruise.tlb.utils;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.io.StringReader;

/**
 * @understands xml reading
 */
public class XmlUtil {
    public static Element domFor(String xmlString) {
        registerNamespaces();
        SAXReader builder = new SAXReader();
        try {
            Document dom = builder.read(new StringReader(xmlString));
            return dom.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("XML could not be understood -> " + xmlString, e);
        }
    }

    private static void registerNamespaces() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("a", "http://www.w3.org/2005/Atom");
        DocumentFactory factory = DocumentFactory.getInstance();
        factory.setXPathNamespaceURIs(map);
    }
}
