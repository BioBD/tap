/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms;

import br.pucrio.biobd.tap.agents.sgbd.ExtractDataFromSQL;
import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.MaterializedView;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.Table;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.SQLDAO;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rpoat
 */
public abstract class GlobalTuningAlgorithm {

    public abstract List<TuningAction> getTuningActions(TuningAction action);

    protected ArrayList<Column> getColumns(MaterializedView action, Table tableMV) {
        ArrayList<Column> columns = new ArrayList<>();
        SQLDAO sqlDao = new SQLDAO();
        SQL sqlCreateMV = sqlDao.getSQL(action.getDdl());
        ExtractDataFromSQL extract = new ExtractDataFromSQL();
        sqlCreateMV = extract.extractData(sqlCreateMV);
        String select = sqlCreateMV.getClause("select");
        int i = 0;
        for (Column column : sqlCreateMV.getFieldsQuery()) {
            if (select.toLowerCase().contains("select *") || select.toLowerCase().contains(column.getName().toLowerCase())) {
                Column currentColumn = Column.createColumn(column.getName(), tableMV);
                currentColumn.setOrder(i++);
                currentColumn.setNotNull(true);
                currentColumn.setPrimaryKey(false);
                currentColumn.setUniqueKey(false);
                currentColumn.setForeignKey(null);
                columns.add(currentColumn);
            }
        }
        return columns;
    }
}
