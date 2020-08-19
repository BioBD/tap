/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.behaviours;

import br.pucrio.biobd.tap.agents.BasicAgent;
import br.pucrio.biobd.tap.agents.libraries.ConnectionSGBD;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.SQLDAO;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.CaptorImplementor;
import jade.core.behaviours.OneShotBehaviour;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class CaptureWorkloadBehaviour extends OneShotBehaviour {

    public BasicAgent agent() {
        return ((BasicAgent) getAgent());
    }

    private Connection connection() {
        ConnectionSGBD conn = new ConnectionSGBD();
        return conn.connection(agent().getClass().getSimpleName());
    }

    public ArrayList<SQL> getLastExecutedSQL() {
        CaptorImplementor captorImp = CaptorImplementor.createNewCaptorImplementor(agent().getClass().getSimpleName());
        return captorImp.readLastExecutedSQL();
    }

    private void insertWorkload(ArrayList<SQL> SQListToBeProcessed) {
        try {
            if (!SQListToBeProcessed.isEmpty()) {
                SQLDAO sqlDao = new SQLDAO();
                sqlDao.setAgent(agent().getClass().getSimpleName());
                for (int i = 0; i < SQListToBeProcessed.size(); ++i) {
                    SQL sql = SQListToBeProcessed.get(i);
                    connection().setAutoCommit(false);
                    if (sqlDao.save(sql)) {
                        connection().commit();
                    } else {
                        connection().rollback();
                    }
                    connection().setAutoCommit(true);
                }
                SQListToBeProcessed.clear();
            }
        } catch (SQLException ex) {
            Log.error(ex);
        }
    }

    public SQL getSQLToBeProcessed(int QueryId, ArrayList<SQL> SQListToBeProcessed) {
        for (int i = 0; i < SQListToBeProcessed.size(); i++) {
            if (SQListToBeProcessed.get(i).getId() == QueryId) {
                return SQListToBeProcessed.get(i);
            }
        }
        return null;
    }

    @Override
    public void action() {
        ArrayList<SQL> SQListToBeProcessed = this.getLastExecutedSQL();
        this.insertWorkload(SQListToBeProcessed);
    }

}
