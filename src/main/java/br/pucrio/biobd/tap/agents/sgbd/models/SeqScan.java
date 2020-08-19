/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import java.util.ArrayList;

/**
 *
 * @author josemariamonteiro
 */
public class SeqScan extends PlanOperation {

    private String tableName;
    private final ArrayList<Filter> filterColumns;
    private long cost;
    private long numberOfRows;

    public SeqScan(ArrayList<Filter> filterColumns) {
        this.filterColumns = filterColumns;
    }

    public String getTableName() {
        return tableName;
    }

    public SeqScan(String tableName, ArrayList<Filter> filterColumns) {
        this.tableName = tableName;
        this.filterColumns = filterColumns;
    }

    public SeqScan(String tableName, ArrayList<Filter> filterColumns, long cost) {
        this.tableName = tableName;
        this.filterColumns = filterColumns;
        this.cost = cost;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ArrayList<Filter> getFilterColumns() {
        return filterColumns;
    }

    public long getCost() {
        return cost;
    }

    public long getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(long numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

}
