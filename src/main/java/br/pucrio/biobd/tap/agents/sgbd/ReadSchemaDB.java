/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.ConnectionSGBD;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.Schema;
import br.pucrio.biobd.tap.agents.sgbd.models.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafael
 */
public class ReadSchemaDB {

    private static Schema schema;
    private static boolean isReading = false;

    public static Schema getSchemaDB() {
        if (schema == null) {
            ConnectionSGBD conn = new ConnectionSGBD();
            Connection connection = conn.connection("readSchemaDB");
            return ReadSchemaDB.getSchemaDB(connection);
        }
        return schema;
    }

    public static Schema getSchemaDB(Connection connection) {
        if (ReadSchemaDB.schema == null) {
            ReadSchemaDB.schema = new Schema();
            ReadSchemaDB.isReading = true;
            if (connection != null) {
                try {
                    Log.title("Reading schema database");
                    String query = Config.getProperty("tableNames_" + Config.getProperty("sgbd"));
                    Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery(query);
                    if (result != null) {
                        while (result.next()) {
                            Table currentTable = new Table();
                            currentTable.setSchema(result.getString(1));
                            currentTable.setName(result.getString(2));
                            currentTable.setNumberRows(result.getInt(3));
                            currentTable.setNumberPages(result.getInt(4));
                            currentTable.setFields(ReadSchemaDB.getColumns(currentTable.getSchema(), currentTable));
                            Log.msg("Table: " + currentTable.getName());
                            Log.msg("Fields: " + currentTable.getFieldsString());
                            schema.tables.add(currentTable);
                        }
                        result.close();
                        statement.close();
                    }
                    Log.endTitle();
                } catch (SQLException e) {
                    Log.error(e);
                }
            }
            ReadSchemaDB.isReading = false;
            updateStatisticsDB(connection);
        }
        while (isReading) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Log.error(ex);
            }
        }
        return schema;
    }

    private static ArrayList<Column> getColumns(String schema, Table table) {
        switch (Config.getProperty("sgbd")) {
            case "postgresql":
                return ReadSchemaDB.getColumnsPostgreSQL(schema, table);
            case "sqlserver":
                return ReadSchemaDB.getColumnsSQLServer(schema, table);
            case "oracle":
                return ReadSchemaDB.getColumnsOracle(schema, table);
            default:
                return ReadSchemaDB.getColumnsSQLServer(schema, table);
        }

    }

    private static double getSelectivitySQLServer(String column, String table) {
        double selectivity = 0;
        try {
            ConnectionSGBD conn = new ConnectionSGBD();
            String sqlSelectivity = Config.getProperty("detailsColumnSeletivity_" + Config.getProperty("sgbd"));
            sqlSelectivity = sqlSelectivity.replace("$column$", column);
            sqlSelectivity = sqlSelectivity.replace("$table$", table);
            PreparedStatement prepSelectivity = conn.connection("ReadSchemaDB").prepareStatement(sqlSelectivity);
            ResultSet fieldsSelectivity = prepSelectivity.executeQuery();
            if (fieldsSelectivity != null) {
                if (fieldsSelectivity.next()) {
                    selectivity = fieldsSelectivity.getDouble(1);
                }
                fieldsSelectivity.close();
                prepSelectivity.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReadSchemaDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return selectivity;
    }

    private static ArrayList<Column> getColumnsPostgreSQL(String schema, Table table) {
        ArrayList<Column> result = new ArrayList<>();
        try {
            String sql = Config.getProperty("detailsColumns_" + Config.getProperty("sgbd"));
            sql = sql.replace("$schema$", schema);
            sql = sql.replace("$table$", table.getName());
            ConnectionSGBD conn = new ConnectionSGBD();
            PreparedStatement preparedStatement = conn.connection("ReadSchemaDB").prepareStatement(sql);
            ResultSet fields = preparedStatement.executeQuery();
            if (fields != null) {
                while (fields.next()) {
                    Column currentColumn = Column.createColumn(fields.getString(2), table);
                    currentColumn.setOrder(fields.getInt(1));
                    currentColumn.setNotNull(fields.getBoolean(3));
                    currentColumn.setType(fields.getString(4));
                    currentColumn.setDomainRestriction(fields.getString(5));
                    currentColumn.setPrimaryKey(fields.getBoolean(6));
                    currentColumn.setUniqueKey(fields.getBoolean(7));
                    currentColumn.setSelectivity(fields.getDouble(13));
                    result.add(currentColumn);
                }
                fields.close();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            Log.error(e);
        }
        return result;
    }

    private static ArrayList<Column> getColumnsSQLServer(String schema, Table table) {
        ArrayList<Column> result = new ArrayList<>();
        try {
            String sql = Config.getProperty("detailsColumns_" + Config.getProperty("sgbd"));
            sql = sql.replace("$schema$", schema);
            sql = sql.replace("$table$", table.getName());
            ConnectionSGBD conn = new ConnectionSGBD();
            PreparedStatement preparedStatement = conn.connection("ReadSchemaDB").prepareStatement(sql);
            ResultSet fields = preparedStatement.executeQuery();
            if (fields != null) {
                while (fields.next()) {
                    Column currentColumn = Column.createColumn(fields.getString(2), table);
                    currentColumn.setOrder(fields.getInt(1));
                    currentColumn.setNotNull(fields.getBoolean(3));
                    currentColumn.setType(fields.getString(4));
                    currentColumn.setDomainRestriction(fields.getString(5));
                    currentColumn.setPrimaryKey(fields.getBoolean(6));
                    currentColumn.setUniqueKey(fields.getBoolean(7));
                    currentColumn.setSelectivity(ReadSchemaDB.getSelectivitySQLServer(fields.getString(2), table.getName()));
                    result.add(currentColumn);
                }
                fields.close();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            Log.error(e);
        }
        return result;
    }

    private static ArrayList<Column> getColumnsOracle(String schema, Table table) {
        ArrayList<Column> result = new ArrayList<>();
        try {
            String sql = Config.getProperty("detailsColumns_" + Config.getProperty("sgbd"));
            sql = sql.replace("$schema$", schema);
            sql = sql.replace("$table$", table.getName());
            ConnectionSGBD conn = new ConnectionSGBD();
            PreparedStatement preparedStatement = conn.connection("ReadSchemaDB").prepareStatement(sql);
            ResultSet fields = preparedStatement.executeQuery();
            if (fields != null) {
                while (fields.next()) {
                    Column currentColumn = Column.createColumn(fields.getString("columnname"), table);
                    currentColumn.setOrder(fields.getInt("ordernum"));
                    currentColumn.setNotNull(fields.getBoolean("isnull"));
                    currentColumn.setType(fields.getString("typefield"));
                    currentColumn.setDomainRestriction(fields.getString("domainrestriction"));
                    currentColumn.setPrimaryKey(fields.getBoolean("primarykey"));
                    currentColumn.setUniqueKey(fields.getBoolean("uniquekey"));
                    result.add(currentColumn);
                }
                fields.close();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            Log.error(e);
        }
        return result;
    }

    private static Column getForeignKeyColumn(ResultSet field, Table table) {
        Column foreignColumn = null;
        try {
            foreignColumn = Column.createColumn(field.getString(10), table);
            foreignColumn.setOrder(field.getInt(9));
            foreignColumn.setType(field.getString(12));
            foreignColumn.setDomainRestriction("");
            foreignColumn.setNotNull(true);
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return foreignColumn;
    }

    private static void updateStatisticsDB(Connection connection) {
        if (Config.getProperty("analyzeTables").equals("1")) {
            try {
                Log.title("Updating database statistics");
                for (Table table : schema.tables) {
                    String query = "ANALYZE " + table.getName() + ";";
                    Log.msg(query);
                    Statement statement = connection.createStatement();
                    statement.executeUpdate(query);
                    statement.close();
                }
                Log.endTitle();
            } catch (SQLException e) {
                Log.error(e);
            }
        }
    }
}
