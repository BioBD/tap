/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Rafael
 */
public class SQLExecution {

    private Date date;
    private Plan plan;

    public Date getDate() {
        return date;
    }

    public Plan getPlan() {
        return this.plan;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public SQLExecution(Date date, Plan plan) {
        this.date = date;
        this.plan = plan;
    }

    public SQLExecution(Date date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        final SQLExecution other = (SQLExecution) obj;
        if (!Objects.equals(this.date, other.date)) {
            return false;
        }
        return true;
    }

}
