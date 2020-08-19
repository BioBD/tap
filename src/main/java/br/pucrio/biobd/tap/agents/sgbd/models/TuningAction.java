/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.TuningActionDAO;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.TuningActionImplementor;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Rafael
 */
public abstract class TuningAction {

    private String ddl;
    private String status;
    private final ArrayList<SQL> sqlList;
    private boolean valid = true;
    private TuningAction parentTuningAction;
    public final ArrayList<String> agents;
    public final ArrayList<String> ids;
    private long profit;

    public void setParentTuningAction(TuningAction parentTuningAction) {
        this.parentTuningAction = parentTuningAction;
    }

    public TuningAction getParentTuningAction() {
        return parentTuningAction;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public abstract String getName();

    public abstract TuningActionDAO getInstanceDAO(String agent);

    public abstract double getCost();

    public abstract void rating(String agent);

    public abstract void debug();

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public TuningAction() {
        this.status = "H";
        this.sqlList = new ArrayList<>();
        this.agents = new ArrayList<>();
        this.ids = new ArrayList<>();
        this.parentTuningAction = null;
    }

    public ArrayList<SQL> getSqlList() {
        return sqlList;
    }

    public SQL getLastSql() {
        if (sqlList.size() > 0) {
            return sqlList.get(sqlList.size() - 1);
        } else {
            return null;
        }
    }

    public int getId() {
        if (!getDdl().isEmpty()) {
            int id = getDdl().toLowerCase().trim().hashCode();
            return id;
        } else {
            return 0;
        }
    }

    public String getDdl() {
        if (ddl == null) {
            return "";
        }
        return Log.removeNl(ddl.replace("  ", " "));
    }

    public abstract String getDdlToExecute();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.ddl);
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
        final TuningAction other = (TuningAction) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        return true;
    }

    public void addSQL(SQL sql) {
        this.removeSqlEquals(sql.getId());
        this.sqlList.add(sql);
    }

    private void removeSqlEquals(int id) {
        for (int i = 0; i < sqlList.size(); i++) {
            if (sqlList.get(i).getId() == id) {
                sqlList.remove(i);
                break;
            }
        }
    }

    public boolean isValid() {
        return this.valid == true
                && this.getCost() > 0
                && TuningActionImplementor.respectRestrictionsSGBD(this);
    }

    public boolean isGlobal() {
        return this.parentTuningAction != null;
    }

    public long getProfit() {
        return profit;
    }

    public void setProfit(long profit) {
        this.profit = profit;
    }

    public abstract boolean selectedBySelectionHeuristic();

    public abstract double getSelectivity();

}
