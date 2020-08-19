/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Rafael
 */
public class PlanSQLServerParser {

    private HashMap<String, PlanOperation> operations;
    private ArrayList<PlanOperation> operationsResult;
    private int j;

    public ArrayList<PlanOperation> getPlanOperations(String plan) {
        this.operationsResult = new ArrayList<>();
        if (plan != null && !plan.isEmpty()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(plan));
                Document document = db.parse(is);
                readOperationsInXMLFile(document.getDocumentElement(), "");
                normalizeOperations();
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(PlanSQLServerParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return operationsResult;
    }

    private static String getValueFromNode(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        String concatResult = "";
        for (int i = 0; i < attributes.getLength(); i++) {
            if (!concatResult.isEmpty()) {
                concatResult += ";";

            }
            concatResult += (attributes.item(i).getNodeName()) + "=" + (attributes.item(i).getNodeValue());
        }
        return concatResult;
    }

    private void processRelOp(Node node, String current) {
        if (node.getNodeName().equals("RelOp")) {
            j++;
            operations.get(current).properties.put(j + "." + node.getNodeName(), getValueFromNode(node));
        }
    }

    private void processIndexScan(Node node, String current) {
        if (node.getNodeName().equals("IndexScan")) {
            j++;
            operations.get(current).properties.put(j + "." + node.getNodeName(), getValueFromNode(node));
        }
    }

    private void processColumnReference(Node node, String current) {
        if (node.getNodeName().equals("ColumnReference")) {
            j++;
            String value = getValueFromNode(node);
            if (!value.isEmpty() && value.toLowerCase().contains("table")) {
                operations.get(current).properties.put(j + "." + node.getNodeName(), value);
            }
        }
    }

    private void processObject(Node node, String current) {
        if (node.getNodeName().equals("Object")) {
            j++;
            operations.get(current).properties.put(j + "." + node.getNodeName(), getValueFromNode(node));
        }
    }

    private void printOperations() {
        System.out.println("*****************************");
        System.out.println("*****************************");
        for (String key : operations.keySet()) {
            System.out.println("*****************************");
            PlanOperation planOp = operations.get(key);
            for (String keyOp : planOp.properties.keySet()) {
                System.out.println(keyOp + " : " + planOp.properties.get(keyOp));
            }
        }
    }

    private void printOperationsResult() {
        System.out.println("*****************************");
        for (PlanOperation planOp : operationsResult) {
            System.out.println("*****************************");
            for (String keyOp : planOp.properties.keySet()) {
                System.out.println(keyOp + " : " + planOp.properties.get(keyOp));
            }
        }
    }

    private void normalizeOperations() {
        operationsResult = new ArrayList<>();
        for (String key : operations.keySet()) {
            PlanOperation planOp = operations.get(key);
            PlanOperation planOpResult = new PlanOperation();
            for (String keyOp : planOp.properties.keySet()) {
                if (keyOp.contains("RelOp")) {
                    String[] opp = planOp.properties.get(keyOp).toString().split(";");
                    for (String operation : opp) {
                        String[] operationValue = operation.split("=");
                        planOpResult.properties.put(operationValue[0], operationValue[1]);
                        switch (operationValue[0]) {
                            case "PhysicalOp":
                                planOpResult.setName(operationValue[1]);
                                break;
                            case "NodeId":
                                planOpResult.setOrder(Integer.valueOf(operationValue[1]));
                                break;
                        }
                    }
                } else {
                    planOpResult.properties.put(keyOp, planOp.properties.get(keyOp));
                }
            }
            operationsResult.add(planOpResult);
        }
    }

    public PlanSQLServerParser() {
        if (operations == null) {
            operations = new HashMap<>();
            j = 0;
        }
    }

    private void readOperationsInXMLFile(Node node, String current) {
        if (node.getNodeName().equals("RelOp")) {
            current = node.getNodeName() + node.getAttributes().getNamedItem("NodeId").getNodeValue();
            PlanOperation plan = new PlanOperation();
            operations.put(current, plan);
        }
        processRelOp(node, current);
        processScalarOperator(node, current);
        processIndexScan(node, current);
        processObject(node, current);
        processColumnReference(node, current);
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                readOperationsInXMLFile(currentNode, current);
            }
        }
    }

    private void processScalarOperator(Node node, String current) {
        if (node.getNodeName().equals("ScalarOperator")) {
            j++;
            String valueScalar = getValueFromNode(node);
            NodeList nodeList = node.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);
                if ((currentNode.getNodeType() == Node.ELEMENT_NODE) && (currentNode.getNodeName().equals("Compare"))) {
                    valueScalar += getValueFromNode(currentNode);
                    break;
                }
            }
            if (!valueScalar.isEmpty()) {
                operations.get(current).properties.put(j + "." + node.getNodeName(), valueScalar);
            }
        }
    }

}
