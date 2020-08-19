/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.PlanImplementor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 *
 * @author Rafael
 */
public class PlanOperation {

    private EnumPlanOperation type;
    private final PlanImplementor planImp;
    private String name;
    private Table table;
    private int order;
    public HashMap<String, Object> properties;

    public PlanOperation() {
        this.properties = new HashMap();
        this.planImp = PlanImplementor.createPlanImplementor();
    }

    public EnumPlanOperation getType() {
        return type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.type = this.planImp.getPlanOperationsType(this);
    }

    public ArrayList<Filter> getFilterColumns(SQL sql) {
        return this.planImp.getFilterColumns(this, sql);
    }

    public Table getTable() {
        if (this.table == null) {
            this.table = this.planImp.getTable(this);
        }
        return this.table;
    }

    public long getNumberOfRowsOfFullScan() {
        return this.planImp.getNumberOfRowsOfFullScan(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + this.order;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlanOperation other = (PlanOperation) obj;
        if (this.order != other.order) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    public void print() {
        print("PlanOperation ");
    }

    public void print(String t) {
        Log.msg(t + "\t Name: " + getName());
        Log.msg(t + "\t Order: " + getOrder());
        properties.forEach((k, v) -> Log.msg(t + "property: " + k + " value:" + v));
        Log.msg("");
    }

    public long getNumRow() {
        return this.planImp.getNumRow(this);
    }

    public String[] getRestrictions() {
        return this.planImp.getRestrictions(this);
    }

    public String getAlias() {
        return this.planImp.getAlias(this);
    }

    public boolean isValid() {
        return this.getTable() != null;
    }

}
