/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.sgbd.ReadSchemaDB;
import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.EnumPlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Plan;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Schema;
import br.pucrio.biobd.tap.agents.sgbd.models.Table;
import br.pucrio.biobd.tap.algoritms.ExtractRestrictions;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author Rafael
 */
public class PlanSQLServerImplementor extends PlanImplementor {

    private static PlanOperation getMajorOperation(ArrayList<PlanOperation> operations) {
        PlanOperation major = null;
        for (PlanOperation operation : operations) {
            if (major == null || (operation.getOrder() < major.getOrder() && !operation.properties.isEmpty())) {
                major = operation;
            }
        }
        return major;
    }

    @Override
    public long getCost(Plan plan) {
        PlanOperation major = getMajorOperation(plan.operations);
        return getCostOperation(major);
    }

    @Override
    public long getNumRow(Plan plan) {
        PlanOperation major = getMajorOperation(plan.operations);
        return getNumRow(major);
    }

    @Override
    public long getNumRow(PlanOperation planOperation) {
        long numRow = 0;
        if (planOperation.properties.containsKey("EstimateRows")) {
            numRow = new BigDecimal(planOperation.properties.get("EstimateRows").toString()).longValue();
        }
        if (numRow > 0) {
            return numRow;
        } else {
            System.out.println("Number of rows: 0");
            return 0;
        }
    }

    @Override
    public long getSizeRow(Plan plan) {
        PlanOperation major = getMajorOperation(plan.operations);
        long numRow = 0;
        if (major.properties.containsKey("AvgRowSize")) {
            numRow = new BigDecimal(major.properties.get("AvgRowSize").toString()).longValue();
        }
        if (numRow > 0) {
            return numRow;
        } else {
            System.out.println("AvgRowSize: 0");
            return 0;
        }
    }

    @Override
    public float getDuration(Plan plan) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getNumberOfRowsOfFullScan(PlanOperation operation) {
        if (operation.properties.get("EstimateRows") != null) {
            return new BigDecimal(operation.properties.get("EstimateRows").toString()).longValue();
        }
        return 0;
    }

    @Override
    public Table getTable(PlanOperation operation) {
        Set<String> ops = operation.properties.keySet();
        String tableNameProperty = null;
        for (String op : ops) {
            if (op.contains("ColumnReference")) {
                tableNameProperty = operation.properties.get(op).toString().split("Table=")[1].replace("[", "").replace("]", "");
            }
        }
        if (tableNameProperty != null) {
            Schema schema = ReadSchemaDB.getSchemaDB();
            for (Table table : schema.tables) {
                if (tableNameProperty.toLowerCase().contains(table.getName().toLowerCase())) {
                    return table;
                }
            }
        }
        return null;
    }

    @Override
    public EnumPlanOperation getPlanOperationsType(PlanOperation planOperation) {
        switch (planOperation.getName().toLowerCase().trim()) {
            case "hash match":
                return EnumPlanOperation.HashJoin;
            case "table scan":
                return EnumPlanOperation.SeqScan;
            case "stream aggregate":
                return EnumPlanOperation.Aggregate;
            default:
                return EnumPlanOperation.Other;
        }
    }

    @Override
    public ArrayList<PlanOperation> extractOperations(String plan) {
        if (!plan.isEmpty()) {
            PlanSQLServerParser parser = new PlanSQLServerParser();
            return parser.getPlanOperations(plan);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public String[] getRestrictions(PlanOperation planOperation) {
        String restrictions = "";
        for (String key : planOperation.properties.keySet()) {
            if (key.contains("ScalarOperator")) {
                restrictions += String.join(";", this.splitRestrictions(planOperation.properties.get(key)));
            }
        }
        return restrictions.split(";");
    }

    @Override
    public String getAlias(PlanOperation planOperation) {
        Table table = this.getTable(planOperation);
        if (planOperation.properties.containsKey("Alias")) {
            String alias = planOperation.properties.get("Alias").toString();
            if (!alias.contains(table.getName())) {
                return alias;
            }
        }
        return "";
    }

    @Override
    public long getCostOperation(PlanOperation operation) {
        long cost = 1;
        if ((operation.properties.containsKey("Total Cost")) && (new Integer(operation.properties.get("Total Cost").toString()) > 0)) {
            cost = new BigDecimal(operation.properties.get("Total Cost").toString()).longValue();
        }
        if ((cost <= 1) && (operation.properties.containsKey("EstimatedTotalSubtreeCost"))) {
            cost = new BigDecimal(operation.properties.get("EstimatedTotalSubtreeCost").toString().replace(".", "")).longValue();
        }
        if (cost <= 1) {
            cost = new BigDecimal(operation.properties.get("EstimateIO").toString().replace(".", "")).longValue();
        }
        return cost;
    }

    public boolean hasColumn(String restriction) {
        Schema schema = ReadSchemaDB.getSchemaDB();
        for (Table table : schema.tables) {
            for (Column field : table.getFields()) {
                if (restriction.toLowerCase().contains(field.getName().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasOperator(String restriction) {
        ExtractRestrictions restrictions = new ExtractRestrictions();
        for (String operator : restrictions.getOperators()) {
            if ((restriction.toLowerCase().contains(operator.toLowerCase()))
                    && (!restriction.toLowerCase().contains("case when"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] splitRestrictions(Object restriction) {
        ArrayList<String> output = new ArrayList<>();
        restriction = restriction.toString().
                replace("ScalarString=", "").
                replace("NOT ", "").
                replace("[sqls_tpch_1gb].[dbo].", "").
                replace("[", "").
                replace("]", "").
                replace("CompareOp=EQ", "").
                replace("CompareOp=LE", "");
        if (hasColumn((String) restriction)) {
            String[] restrictions = restriction.toString().split(" AND | OR | and | or ");
            for (int i = 0; i < restrictions.length; i++) {
                if (hasOperator(restrictions[i])) {
                    if (restrictions[i].contains(" > '")) {
                        restrictions[i] = restrictions[i].replace("'", "");
                    }
                    if (!restrictions[i].contains(" in ")) {
                        restrictions[i] = restrictions[i].replace(")", "").replace("(", "");
                    }
                    output.add(restrictions[i]);
                }
            }
            return output.toArray(new String[0]);
        }
        return new String[0];
    }
}
