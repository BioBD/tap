/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class ConnectionSGBD {

    private static HashMap connectionList = new HashMap();

    public Connection connection(String agent) {
        if (!connectionList.containsKey(agent)) {
            connectionList.put(agent, connect());
        } else {
            try {
                if (((Connection) connectionList.get(agent)).isClosed()) {
                    connectionList.remove(agent);
                    connectionList.put(agent, connect());
                }
            } catch (SQLException ex) {
                Log.msg(ex);
            }
        }
        return (Connection) connectionList.get(agent);
    }

    public Connection resetConnection(String agent) {
        if (connectionList.containsKey(agent)) {
            try {
                Connection conn = (Connection) connectionList.get(agent);
                conn.close();
                connectionList.remove(agent);
            } catch (SQLException ex) {
                Log.error(ex);
            }
        }
        connectionList.put(agent, connect());
        return (Connection) connectionList.get(agent);
    }

    private Connection connect() {
        Connection connection = null;
        try {
            switch (Config.getProperty("sgbd")) {
                case "sqlserver":
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    connection = DriverManager.getConnection(Config.getProperty("urlSQLServer") + "user=" + Config.getProperty("userSQLServer") + ";password=" + Config.getProperty("pwdSQLServer") + ";databaseName=" + Config.getProperty("databaseName"));
                    Log.msg("Conectado ao bd " + Config.getProperty("urlSQLServer") + ":" + Config.getProperty("databaseName"));
                    break;
                case "postgresql":
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection(Config.getProperty("urlPostgres") + Config.getProperty("databaseName"), Config.getProperty("userPostgres"), Config.getProperty("pwdPostgres"));
                    Log.msg("Conectado ao bd " + Config.getProperty("urlPostgres") + ":" + Config.getProperty("databaseName"));
                    break;
                case "oracle":
                    connection = DriverManager.getConnection(Config.getProperty("urlOracle") + Config.getProperty("tnsOracle"), Config.getProperty("userOracle"), Config.getProperty("pwdOracle"));
                    Log.msg("Conectado ao TNS " + Config.getProperty("tnsOracle") + " e DATABASE " + Config.getProperty("urlOracle") + Config.getProperty("databaseName"));
                    break;
                default:
                    throw new UnsupportedOperationException("Atributo SGBD do arquivo de parâmetros (.properties) nao foi atribuido corretamente.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            Log.error(e);
        }
        return connection;
    }

    public void closeConnection(String agent) throws SQLException {
        if (connectionList.containsKey(agent)) {
            Connection connection = (Connection) connectionList.get(agent);
            connectionList.remove(agent);
            connection.close();
        }
    }

}
