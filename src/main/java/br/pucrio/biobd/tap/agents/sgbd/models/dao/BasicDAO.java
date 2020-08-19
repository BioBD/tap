/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.dao;

import br.pucrio.biobd.tap.agents.libraries.ConnectionSGBD;
import java.sql.Connection;

/**
 *
 * @author Rafael
 */
public class BasicDAO {

    protected String agent;

    public void setAgent(String agent) {
        this.agent = agent;
    }

    protected Connection connection() {
        ConnectionSGBD conn = new ConnectionSGBD();
        return conn.connection(agent);
    }

}
