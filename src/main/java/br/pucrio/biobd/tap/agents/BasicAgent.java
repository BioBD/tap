/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents;

import br.pucrio.biobd.tap.agents.libraries.ConnectionSGBD;
import br.pucrio.biobd.tap.agents.sgbd.models.Schema;
import jade.core.Agent;
import java.sql.Connection;

/**
 *
 * @author Rafael
 */
public class BasicAgent extends Agent {

    private final ConnectionSGBD connection;
    public Schema schema;

    public BasicAgent() {
        this.connection = new ConnectionSGBD();
    }

    public Connection getConnection() {
        return this.connection.connection(this.getClass().getSimpleName());
    }

}
