/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import br.pucrio.biobd.tap.agents.libraries.CaptorExecutionPlan;
import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.MaterializedViewDAO;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.TuningActionDAO;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.TuningActionImplementor;

/**
 *
 * @author Rafael
 */
public class MaterializedView extends TuningAction {

    private Plan hypoPlan;
    private boolean subQuery = false;

    public boolean isSubQuery() {
        return subQuery;
    }

    public void setSubQuery(boolean subQuery) {
        this.subQuery = subQuery;
    }

    public MaterializedView() {
        super();
    }

    private void setHypoPlan(Plan hypoPlan) {
        if (hypoPlan == null || hypoPlan.operations.isEmpty()) {
            this.setValid(false);
        } else {
            this.hypoPlan = hypoPlan;
        }
    }

    @Override
    public double getCost() {
        if (this.hypoPlan != null) {
            return (this.getHypoNumPages() * 2) + this.hypoPlan.getCost();
        } else {
            return 0;
        }

    }

    @Override
    public String getName() {
        return "MV_TAP_Q" + this.getComments().replace("/* TPC_H Query", "").replace("*/", "").trim().toUpperCase();
    }

    public String getSchema() {
        return Config.getProperty("schemaMaterializedView");
    }

    @Override
    public void rating(String agent) {
        if (TuningActionImplementor.respectRestrictionsSGBD(this)) {
            CaptorExecutionPlan captorPlan = new CaptorExecutionPlan(agent);
            Plan hypoPlanCaptured = captorPlan.getPlanExecution(super.getDdl());
            this.setHypoPlan(hypoPlanCaptured);
        }
    }

    private double getFillFactory() {

        return Double.valueOf(Config.getProperty("fillfactory" + Config.getProperty("sgbd")));
    }

    private double getPageSize() {
        return Double.valueOf(Config.getProperty("pagesize" + Config.getProperty("sgbd")));
    }

    public long getNumRow() {
        if (this.hypoPlan != null) {
            return this.hypoPlan.getNumRow();
        } else {
            return 0;
        }
    }

    private long getSizeRow() {
        if (this.hypoPlan != null) {
            return this.hypoPlan.getSizeRow();
        } else {
            return 0;
        }
    }

    public long getHypoNumPages() {
        double sizeByTuple = getNumRow() * getSizeRow();
        double sizeByTupleByFillfactor = sizeByTuple * getFillFactory();
        double numPages = sizeByTupleByFillfactor / getPageSize();
        numPages += numPages * 0.1;
        if (numPages < 1) {
            return 1;
        } else {
            return Math.round(numPages);
        }
    }

    @Override
    public void debug() {
    }

    @Override
    public String getDdlToExecute() {
        if (this.getSqlList().size() > 0 && !super.getDdl().isEmpty()) {
            String ddl = Config.getProperty("DDLCreateMV_" + Config.getProperty("sgbd"));
            ddl = ddl.replace("$nameMV$", this.getName());
            ddl = ddl.replace("$sqlMV$", super.getDdl());
            return ddl;
        } else {
            return null;
        }
    }

    @Override
    public TuningActionDAO getInstanceDAO(String agent) {
        TuningActionDAO tuningActionDAO = new MaterializedViewDAO();
        tuningActionDAO.setAgent(agent);
        return tuningActionDAO;
    }

    @Override
    public boolean selectedBySelectionHeuristic() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getSelectivity() {
        return 1;
    }

    public String getComments() {
        if (this.getDdl() != null) {
            int ini = this.getDdl().indexOf("/*");
            if (ini >= 0) {
                int end = this.getDdl().substring(ini).indexOf("*/") + ini + 2;
                return this.getDdl().substring(ini, end).trim() + " ";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

}
