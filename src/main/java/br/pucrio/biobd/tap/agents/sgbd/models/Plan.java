/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import br.pucrio.biobd.tap.agents.sgbd.IPlan;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.PlanImplementor;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class Plan implements IPlan {

    public ArrayList<PlanOperation> operations;
    private boolean hypotetical = false;
    private final PlanImplementor planImp;

    public ArrayList<PlanOperation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<PlanOperation> operations) {
        this.operations = operations;
    }

    public boolean isHypotetical() {
        return hypotetical;
    }

    public void setHypotetical(boolean hypotetical) {
        this.hypotetical = hypotetical;
    }

    public Plan() {
        this.operations = new ArrayList<>();
        this.planImp = PlanImplementor.createPlanImplementor();
    }

    public void print(String t) {
        for (PlanOperation op : operations) {
            op.print(t);
        }
    }

    @Override
    public long getCost() {
        return this.planImp.getCost(this);
    }

    @Override
    public long getNumRow() {
        return this.planImp.getNumRow(this);
    }

    @Override
    public long getSizeRow() {
        return this.planImp.getSizeRow(this);
    }

    @Override
    public float getDuration() {
        return this.planImp.getDuration(this);
    }

    public ArrayList<PlanOperation> getOperationsByType(EnumPlanOperation operationType) {
        return this.planImp.getOperationsByType(this, operationType);

    }

    public void addAllOperations(String operations) {
        this.operations.addAll(this.planImp.extractOperations(operations));
    }
}
