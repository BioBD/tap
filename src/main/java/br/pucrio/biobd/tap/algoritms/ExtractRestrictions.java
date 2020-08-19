/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms;

import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.EnumPlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Restriction;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.SQLImplementor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rpoat
 */
public class ExtractRestrictions {

    public Column getColumn(SQL sql, String name, PlanOperation operation) {
        SQLImplementor sqlImp = SQLImplementor.createNewSQLImplementor();
        if (name.equals("n_name")) {
            for (Column column : sql.getFieldsQuery()) {
                if (!operation.getAlias().isEmpty() && !name.contains(operation.getAlias())) {
                    name = operation.getAlias() + "." + name;
                }
                if (sqlImp.containsFieldOrTable(name, column.getName())) {
                    if (name.equals(column.getCompleteName())) {
                        return column;
                    } else {
                        column.setAlias(name.replace(".", "_"));
                        return column;
                    }
                }
            }
        } else {
            for (Column column : sql.getFieldsQuery()) {
                if (!operation.getAlias().isEmpty() && !name.contains(operation.getAlias())) {
                    name = operation.getAlias() + "." + name;
                }
                if (sqlImp.containsFieldOrTable(name, column.getName())) {
                    if (name.equals(column.getCompleteName())) {
                        return column;
                    } else {
                        column.setAlias(name.replace(".", "_"));
                        return column;
                    }
                }
            }
        }
        if (sql.getParentSQL() != null) {
            for (Column column : sql.getParentSQL().getFieldsQuery()) {
                if (sqlImp.containsFieldOrTable(name, column.getName())) {
                    if (name.equals(column.getCompleteName())) {
                        return column;
                    } else {
                        column.setAlias(name.replace(".", "_"));
                        return column;
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<Restriction> getRestrictionsByOperationType(SQL sql, EnumPlanOperation operationType) {
        ArrayList<Restriction> restrictions = new ArrayList<>();
        if (operationType.equals(EnumPlanOperation.Join)) {
            restrictions.addAll(this.getRestrictionsByOperationType(sql, EnumPlanOperation.HashJoin));
            restrictions.addAll(this.getRestrictionsByOperationType(sql, EnumPlanOperation.MergeJoin));
            return restrictions;
        } else {
            for (PlanOperation operation : sql.getOperationsByType(operationType)) {
                for (String condition : operation.getRestrictions()) {
                    List<String> condParts = this.splitConditions(condition);
                    if (condParts.size() > 0) {
                        Column columnA = this.getColumn(sql, condParts.get(0), operation);
                        Column columnB = this.getColumn(sql, condParts.get(2), operation);
                        if (columnA != null) {
                            Restriction restriction;
                            if (columnB != null) {
                                restriction = new Restriction(columnA, condParts.get(1), columnB);
                            } else {
                                restriction = new Restriction(columnA, condParts.get(1), condParts.get(2));
                                restriction = this.processArrayValues(restriction);
                            }
                            restriction.setNumRows(operation.getNumRow());
                            if (!restrictions.contains(restriction)) {
                                restrictions.add(restriction);
                            }
                        }
                    }
                }
            }
            return restrictions;
        }
    }

    public List<String> splitConditions(String condition) {
        List<String> partsList = new ArrayList<>();
        for (String operator : this.getOperators()) {
            String[] parts = condition.split(operator);
            if (parts.length > 1) {
                partsList.add(parts[0].trim());
                partsList.add(operator.trim());
                partsList.add(parts[1].trim());
                return partsList;
            }
        }
        return partsList;
    }

    public ArrayList<String> getOperators() {
        ArrayList<String> operators = new ArrayList<>();
        operators.add(">=");
        operators.add("<=");
        operators.add("<>");
        operators.add("!=");
        operators.add("<");
        operators.add("=");
        operators.add(">");
        operators.add("ilike");
        operators.add("not ilike");
        operators.add("like");
        operators.add("not like");
        return operators;
    }

    public ArrayList<Restriction> mergeRestrictions(ArrayList<Restriction> hashRestrictions, ArrayList<Restriction> seqRestrictions) {
        ArrayList<Restriction> allRestrictions = new ArrayList<>();
        allRestrictions.addAll(hashRestrictions);
        for (Restriction seqScanRestriction : seqRestrictions) {
            if (!allRestrictions.contains(seqScanRestriction)) {
                allRestrictions.add(seqScanRestriction);
            }
        }
        return allRestrictions;
    }

    public ArrayList<Restriction> removeDuplicateRestrictions(ArrayList<Restriction> allRestrictions) {
        ArrayList<Restriction> restrictions = new ArrayList<>();
        for (Restriction seqScanRestriction : allRestrictions) {
            if (!restrictions.contains(seqScanRestriction)) {
                restrictions.add(seqScanRestriction);
            }
        }
        return restrictions;
    }

    public Restriction processArrayValues(Restriction restriction) {
        if (restriction.getValue().toString().toLowerCase().contains("any ")) {
            String[] parts = restriction.getValue().toString().split(" AND ");
            String result = "";
            for (String part : parts) {
                if (!result.isEmpty()) {
                    result += " AND ";
                }
                if (part.toLowerCase().contains("any ")) {
                    String value = part.replace("'", "");
                    value = value.replace("ANY ", "in ");
                    value = value.replace("{", "('");
                    value = value.replace(",", "','");
                    value = value.replace("}", "')");
                    value = value.replace("\\", "");
                    result += value;
                } else {
                    result += part;
                }
            }
            return new Restriction(restriction.getColumn(), "", result);
        } else {
            return restriction;
        }

    }

    public ArrayList<Restriction> getPrimaryKeyRestrictions(SQL sql) {
        ArrayList<Restriction> restrictions = new ArrayList<>();
        for (Column field : sql.getFieldsQuery()) {
            if (field.isPrimaryKey()) {
                restrictions.add(new Restriction(field, ">", "0"));
            }
        }

        return restrictions;
    }

}
