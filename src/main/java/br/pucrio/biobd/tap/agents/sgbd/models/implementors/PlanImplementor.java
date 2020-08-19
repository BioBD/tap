/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.EnumPlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Filter;
import br.pucrio.biobd.tap.agents.sgbd.models.Plan;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.Table;
import br.pucrio.biobd.tap.algoritms.ExtractRestrictions;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public abstract class PlanImplementor {

    public abstract long getCost(Plan plan);

    public abstract long getCostOperation(PlanOperation operation);

    public abstract long getNumRow(Plan plan);

    public abstract long getNumRow(PlanOperation operation);

    public abstract long getSizeRow(Plan plan);

    public abstract float getDuration(Plan plan);

    public abstract long getNumberOfRowsOfFullScan(PlanOperation operation);

    private String getFilterType(String filterPredicates) {
        if (filterPredicates.contains(">") || filterPredicates.contains("<")) {
            return "theta";
        } else {
            return "equi";
        }
    }

    public ArrayList<Filter> getFilterColumns(PlanOperation operation, SQL sql) {
        ArrayList<Filter> filters = new ArrayList<>();
        ExtractRestrictions extract = new ExtractRestrictions();
        for (String condition : operation.getRestrictions()) {
            List<String> condParts = extract.splitConditions(condition);
            if (condParts.size() > 0) {
                Column columnA = extract.getColumn(sql, condParts.get(0), operation);
                Column columnB = extract.getColumn(sql, condParts.get(2), operation);

                if (columnA != null) {
                    Filter filter = new Filter(columnA.getName(), columnA.getTable());
                    filter.setFilterType(getFilterType(condParts.get(1)));
                    filters.add(filter);
                }
                if (columnB != null) {
                    Filter filter = new Filter(columnB.getName(), columnB.getTable());
                    filter.setFilterType(getFilterType(condParts.get(1)));
                    filters.add(filter);
                }
            }
        }
        return filters;
    }

    public abstract Table getTable(PlanOperation operation);

    public static PlanImplementor createPlanImplementor() {
        switch (Config.getProperty("sgbd")) {
            case "postgresql":
                return new PlanPostgreSQLImplementor();
            case "oracle":
                return new PlanOracleImplementor();
            case "sqlserver":
                return new PlanSQLServerImplementor();
            default:
                return null;

        }
    }

    public ArrayList<PlanOperation> getOperationsByType(Plan plan, EnumPlanOperation operationType) {
        ArrayList<PlanOperation> operations = new ArrayList<>();
        if (operationType.equals(EnumPlanOperation.Join)) {
            operations.addAll(this.getOperationsByType(plan, EnumPlanOperation.HashJoin));
            operations.addAll(this.getOperationsByType(plan, EnumPlanOperation.MergeJoin));
            return operations;
        } else {
            for (PlanOperation operation : plan.operations) {
                if (operation.getType().equals(operationType) && operation.isValid()) {
                    operations.add(operation);
                } else {
                }
            }
            return operations;
        }
    }

    public abstract EnumPlanOperation getPlanOperationsType(PlanOperation planOperation);

    public abstract ArrayList<PlanOperation> extractOperations(String plan);

    public abstract String[] getRestrictions(PlanOperation planOperation);

    public abstract String getAlias(PlanOperation planOperation);

    public abstract String[] splitRestrictions(Object restriction);
}
