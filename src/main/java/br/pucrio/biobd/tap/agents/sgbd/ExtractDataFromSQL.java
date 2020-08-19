/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd;

import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.Schema;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.SQLImplementor;

/**
 *
 * @author Rafael
 */
public class ExtractDataFromSQL {

    private SQLImplementor SQLImp;

    public ExtractDataFromSQL() {
        this.SQLImp = SQLImplementor.createNewSQLImplementor();
    }

    public SQL extractData(SQL sql) {
        if (SQLImp != null) {
            Schema schema = ReadSchemaDB.getSchemaDB();
            sql.setFieldsQuery(SQLImp.extractColumns(sql, schema));
            sql.setTablesQuery(SQLImp.extractTables(sql, schema));
            SQLImp.fixSQLSubQuery(sql);
            sql.setClauses(SQLImp.extractClauses(sql));
        }
        return sql;
    }

}
