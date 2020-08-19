/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.sgbd.ReadSchemaDB;
import br.pucrio.biobd.tap.agents.sgbd.models.EnumPlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Plan;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Schema;
import br.pucrio.biobd.tap.agents.sgbd.models.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Rafael
 */
public class PlanOracleImplementor extends PlanImplementor {

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
        if (major.properties.containsKey("cardinality")) {
            return Long.valueOf(major.properties.get("cardinality").toString());
        } else {
            return 0;
        }
    }

    @Override
    public long getSizeRow(Plan plan) {
        PlanOperation major = getMajorOperation(plan.operations);
        if (major.properties.get("bytes") == null || major.properties.get("bytes").equals("null")) {
            return 0;
        }
        long numRows = getNumRow(plan);
        long totalSize = Long.valueOf(major.properties.get("bytes").toString()) * 8;
        if (totalSize > 0 && numRows > 0) {
            long total = totalSize / numRows;
            if (total <= 0) {
                total = Long.valueOf(major.properties.get("bytes").toString()) * 8;
            }
            return total;
        } else {
            return 0;
        }
    }

    @Override
    public float getDuration(Plan plan) {
        PlanOperation major = getMajorOperation(plan.operations);
        if (major.properties.containsKey("time") && major.properties.get("time").equals("null")) {
            major.properties.replace("time", "0");
        }
        if (major.properties.containsKey("time") && !major.properties.get("time").equals("null")) {
            float elapsed = Float.valueOf(major.properties.get("time").toString());
            if (elapsed > 1000000) {
                return elapsed / 1000000;
            }
            if (elapsed > 0) {
                return elapsed;
            }
        }
        return 0;
    }

    @Override
    public long getNumberOfRowsOfFullScan(PlanOperation operation) {
        if (operation.properties.get("cardinality") != null) {
            return new BigDecimal(operation.properties.get("cardinality").toString()).longValue();
        }
        return 0;
    }

    private static String getPropertyByPartName(PlanOperation operation, String partName) {
        for (Map.Entry pair : operation.properties.entrySet()) {
            if (pair.getKey().toString().trim().toLowerCase().equals(partName.toLowerCase().trim())) {
                return (String) pair.getValue();
            }
        }
        return null;
    }

    @Override
    public Table getTable(PlanOperation operation) {
        String tableNameProperty = (String) getPropertyByPartName(operation, "object_name");
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
            case "hash join":
                return EnumPlanOperation.HashJoin;
            case "table access":
                return EnumPlanOperation.SeqScan;
            case "aggregate":
                return EnumPlanOperation.Aggregate;
            default:
                return EnumPlanOperation.Other;
        }
    }

    @Override
    public ArrayList<PlanOperation> extractOperations(String plan) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getRestrictions(PlanOperation planOperation) {
        switch (planOperation.getName().toLowerCase().trim()) {
            case "hash join":
                return this.splitRestrictions(planOperation.properties.get("access_predicates"));
            case "table access":
                return this.splitRestrictions(planOperation.properties.get("filter_predicates"));
            default:
                return this.splitRestrictions(planOperation.properties.get("filter_predicates"));
        }
    }

    @Override
    public String[] splitRestrictions(Object restriction) {
        if ((restriction == null) || (restriction.toString().equals("null"))) {
            restriction = "";
        }
        restriction = restriction.toString().
                replace("sum(", "").
                replace("year,", "").
                replace("extract(", "").
                replace("\"", "").
                replace("sum(", "").
                replace("count(", "").
                replace("substr(", "").
                replace("max(", "").
                replace("min(", "").
                replace("avg(", "");
        String[] restrictions = restriction.toString().split(" AND | OR | and | or ");
        for (int i = 0; i < restrictions.length; i++) {
            if (restrictions[i].contains(" > '")) {
                restrictions[i] = restrictions[i].replace("'", "");
            }
            if (!restrictions[i].contains(" in ")) {
                restrictions[i] = restrictions[i].replace(")", "").replace("(", "");
            }
        }
        return restrictions;
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
        operation.print();
        if (operation.properties.get("cost") != null) {
            return Long.valueOf(operation.properties.get("cost").toString());
        } else {
            return 0;
        }
    }

    @Override
    public long getNumRow(PlanOperation operation) {
        if (operation.properties.containsKey("cardinality")) {
            return Long.valueOf(operation.properties.get("cardinality").toString());
        } else {
            return 0;
        }
    }
}
