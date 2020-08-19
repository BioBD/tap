/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Rafael
 */
public class PlanPostgreSQLParser {

    private HashMap<String, PlanOperation> operations;
    private ArrayList<PlanOperation> operationsResult;
    private int j;

    public PlanPostgreSQLParser() {
        if (operations == null) {
            operations = new HashMap<>();
            j = 0;
        }
    }

    public ArrayList<PlanOperation> getPlanOperations(String plan) {
        this.operationsResult = new ArrayList<>();
        if (plan != null && !plan.isEmpty()) {
            String lines[] = plan.split("\\r?\\n");
            int i = 0;
            PlanOperation operation = new PlanOperation();
            for (int j = 2; j < lines.length; j++) {
                if (lines[j].trim().equals("\"Plans\": [") || lines[j].trim().equals("},")) {
                    operation.setOrder(i++);
                    if (operation.getName() == null) {
                        operation.setName("Major");
                    }
                    this.operationsResult.add(operation);
                    operation = new PlanOperation();
                    operation.setOrder(i++);
                    j = j + 2;
                }

                lines[j] = this.cleanValues(lines[j].trim());
                if (lines[j].contains(": ")) {
                    String[] values = lines[j].split(": ");
                    if (!values[1].trim().equals("{") && !values[1].trim().equals("[")) {
                        operation.properties.put(values[0].trim(), values[1].trim());
                        if (values[0].equals("Node Type")) {
                            operation.setName(values[1]);
                        }
                    }
                }
            }
            if (!operationsResult.contains(operation)) {
                operationsResult.add(operation);
            }
        }
        return operationsResult;
    }

    private String cleanValues(String value) {
        if (!value.toLowerCase().contains(" any ")) {
            value = value.replace(",", "");
        }
        return value.
                replace("$", "").
                replace("*", "").
                replace("::text", "").
                replace("_1", "").
                replace("~~", "=").
                replace("\"", "").
                replace("::date", "").
                replace("::bpchar[]", "").
                replace("::integer[]", "").
                replace("::bpchar", "").
                replace("::timestamp", "").
                replace("::numeric", "").
                replace("without time zone", "");
    }
}
