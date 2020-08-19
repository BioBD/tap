/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.libraries.Log;
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
public class PlanPostgreSQLImplementor extends PlanImplementor {

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
        if (planOperation.properties.containsKey("Plan Rows")) {
            numRow = new BigDecimal(planOperation.properties.get("Plan Rows").toString()).longValue();
        } else if (planOperation.properties.containsKey("Actual Rows")) {
            numRow = new BigDecimal(planOperation.properties.get("Actual Rows").toString()).longValue();
        }
        if (numRow > 0) {
            return numRow;
        } else {
            return numRow;
        }
    }

    @Override
    public long getSizeRow(Plan plan) {
        PlanOperation major = getMajorOperation(plan.operations);
        if (!major.properties.containsKey("Plan Width")) {
            major.print("Plan Width");
        }
        return new BigDecimal(major.properties.get("Plan Width").toString()).longValue();
    }

    @Override
    public float getDuration(Plan plan) {
        PlanOperation major = getMajorOperation(plan.operations);
        if (!major.properties.containsKey("Actual Total Time")) {
            major.print("Actual Total Time");
        }
        return new BigDecimal(major.properties.get("Actual Total Time").toString()).floatValue() / 10;
    }

    @Override
    public long getNumberOfRowsOfFullScan(PlanOperation operation) {
        if (operation.properties.get("Plan Rows") != null) {
            return new BigDecimal(operation.properties.get("Plan Rows").toString()).longValue();
        }
        return 0;
    }

    @Override
    public ArrayList<PlanOperation> extractOperations(String plan) {
        if (!plan.isEmpty()) {
            PlanPostgreSQLParser parser = new PlanPostgreSQLParser();
            return parser.getPlanOperations(plan);
        } else {
            return new ArrayList<>();
        }
    }

    private static PlanOperation getMajorOperation(ArrayList<PlanOperation> operations) {
        PlanOperation major = null;
        for (PlanOperation operation : operations) {
            if (major == null || (operation.getOrder() < major.getOrder() && !operation.properties.isEmpty())) {
                major = operation;
            }
        }
        return major;
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
        String tableNameProperty = (String) getPropertyByPartName(operation, "Relation Name");
        if (tableNameProperty == null) {
            switch (operation.getName()) {
                case "Merge Join":
                    tableNameProperty = (String) getPropertyByPartName(operation, "Merge Cond");
                    break;
                case "Aggregate":
                    tableNameProperty = (String) getPropertyByPartName(operation, "Filter");
                    if (tableNameProperty == null) {
                        tableNameProperty = (String) getPropertyByPartName(operation, "Group Key");
                    }
                    break;
                case "Hash Join":
                    tableNameProperty = (String) getPropertyByPartName(operation, "Hash Cond");
                    break;
                default:
                    operation.print("object TableName");
                    Log.error("Table name not found in operation " + operation.getName());
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
    public EnumPlanOperation getPlanOperationsType(PlanOperation plan) {
        switch (plan.getName().toLowerCase().trim()) {
            case "hash join":
                return EnumPlanOperation.HashJoin;
            case "seq scan":
                return EnumPlanOperation.SeqScan;
            case "merge join":
                return EnumPlanOperation.MergeJoin;
            case "aggregate":
                return EnumPlanOperation.Aggregate;
            default:
                return EnumPlanOperation.Other;
        }
    }

    @Override
    public String[] getRestrictions(PlanOperation planOperation) {
        switch (planOperation.getType()) {
            case MergeJoin:
                if (planOperation.properties.containsKey("Merge Cond")) {
                    return this.splitRestrictions(planOperation.properties.get("Merge Cond"));
                }
                break;
            case HashJoin:
                if (planOperation.properties.containsKey("Hash Cond")) {
                    return this.splitRestrictions(planOperation.properties.get("Hash Cond"));
                }
                break;
            case SeqScan:
            case Aggregate:
                if (planOperation.properties.containsKey("Filter")) {
                    return this.splitRestrictions(planOperation.properties.get("Filter"));
                }
        }
        return new String[0];
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
        long cost = 0;
        if (operation.properties.containsKey("Total Cost")) {
            cost = new BigDecimal(operation.properties.get("Total Cost").toString()).longValue();
        }
        return cost;
    }

    @Override
    public String[] splitRestrictions(Object restriction) {
        restriction = restriction.toString().
                replace("dateadd(mm, 3, cast(", "").
                replace("dateadd(mm, 3, cast(", "").
                replace("dateadd(dd, -90, cast(", "").
                replace("as datetime))", "").
                replace("cast(", "").
                replace("dateadd(", "").
                replace("sum(", "").
                replace("count(", "").
                replace("substr(", "").
                replace("_1", "").
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

}
