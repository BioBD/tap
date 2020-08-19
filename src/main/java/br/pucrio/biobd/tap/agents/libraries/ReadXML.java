/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Rafael
 */
public class ReadXML {

    public Document readXMLFile(String path) {
        try {
            File file = new File(path);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            document.getDocumentElement().normalize();
            return document;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(ReadXML.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<String> getNodeValues(String pathFile, String nodeListName, String attributeName, String attributeValue) {
        Document doc = this.readXMLFile(pathFile);
        NodeList nodeList = doc.getElementsByTagName(nodeListName);
        return this.getNodes(nodeList, attributeName, attributeValue);
    }

    public List<String> getNodeValuesActive(String pathFile, String nodeListName, String attributeName, String attributeValue) {
        Document doc = this.readXMLFile(pathFile);
        NodeList nodeList = doc.getElementsByTagName(nodeListName);
        List<String> nodes = this.getNodes(nodeList, attributeName, attributeValue);
        return nodes;
    }

    private List<String> getNodes(NodeList nodeList, String attributeName, String attributeValue) {
        ArrayList<String> nodesValues = new ArrayList<>();
        String nodeValue = "";
        boolean active = false;
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                if (tempNode.hasAttributes()) {
                    NamedNodeMap nodeMap = tempNode.getAttributes();
                    for (int i = 0; i < nodeMap.getLength(); i++) {
                        Node node = nodeMap.item(i);
                        if (node.getNodeName().equals("active")) {
                            active = node.getNodeValue().equals("1");
                        }
                        if (node.getNodeName().equals(attributeName) && node.getNodeValue().equals(attributeValue)) {
                            nodeValue = tempNode.getTextContent();
                        }
                    }
                    if (!nodeValue.isEmpty() && active) {
                        nodesValues.add(nodeValue);
                    }
                    active = false;
                    nodeValue = "";
                }
                if (tempNode.hasChildNodes()) {
                    nodesValues.addAll(getNodes(tempNode.getChildNodes(), attributeName, attributeValue));
                }
            }
        }
        return nodesValues;
    }

    public Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes()));
    }

}
