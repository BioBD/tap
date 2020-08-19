/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

/**
 *
 * @author josemariamonteiro
 */
public class Filter extends Column {

    private String filterType;

    public Filter(String name, Table table) {
        super(name, table);
        for (Column fieldToCopy : table.getFields()) {
            if (fieldToCopy.getName().equals(name)) {
                setOrder(fieldToCopy.getOrder());
                setNotNull(fieldToCopy.isNotNull());
                setType(fieldToCopy.getType());
                setDomainRestriction(fieldToCopy.getDomainRestriction());
                setPrimaryKey(fieldToCopy.isPrimaryKey());
                setUniqueKey(fieldToCopy.isUniqueKey());
                setSelectivity(fieldToCopy.getSelectivity());
                break;
            }
        }
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

}
