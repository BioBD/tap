/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.ConnectionSGBD;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.ReadSchemaDB;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.Schema;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Rafael
 */
public abstract class CaptorImplementor {

    protected final Schema schema;
    protected ArrayList<SQL> SQListToBeProcessed;
    protected Connection connection;
    protected final String agent;
    private SQLImplementor SQLImp;

    public static CaptorImplementor createNewCaptorImplementor(String agent) {
        CaptorImplementor captorImp;
        switch (Config.getProperty("sgbd")) {
            case "postgresql":
                captorImp = new CaptorPostgreSQLImplementor(agent);
                break;
            case "oracle":
                captorImp = new CaptorOracleImplementor(agent);
                break;
            default:
                captorImp = new CaptorSQLServerImplementor(agent);
                break;
        }
        return captorImp;
    }

    public CaptorImplementor(String agent) {
        this.SQListToBeProcessed = new ArrayList<>();
        this.agent = agent;
        this.connection = connection();
        this.schema = ReadSchemaDB.getSchemaDB(connection);
        this.SQLImp = SQLImplementor.createNewSQLImplementor();
    }

    private Connection connection() {
        ConnectionSGBD conn = new ConnectionSGBD();
        return conn.connection(agent);
    }

    private Connection resetConnection() {
        ConnectionSGBD conn = new ConnectionSGBD();
        return conn.resetConnection(agent);
    }

    public ArrayList<SQL> readLastExecutedSQL() {
        if (Config.getProperty("readWorkloadFromFile").equals("1")) {
            return this.readLastExecutedSQLFromFile();
        } else {
            return this.readLastExecutedSQLFromSGBD();
        }
    }

    private ArrayList<SQL> readLastExecutedSQLFromSGBD() {
        try {
            String query = Config.getProperty("captureCurrentQueries_" + Config.getProperty("sgbd"));
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, Config.getProperty("databaseName"));
            ResultSet queriesResult = preparedStatement.executeQuery();
            while (queriesResult.next()) {
                String currentQuery = queriesResult.getString("sql");
                if (this.isQueryValid(currentQuery)) {
                    SQL sql = SQLImp.createSQL(currentQuery, queriesResult.getTimestamp("start_time"), schema, SQListToBeProcessed);
                    if (sql.isValid() && !SQListToBeProcessed.contains(sql)) {
                        SQListToBeProcessed.add(sql);
                    }
                }
            }
            queriesResult.close();
            preparedStatement.close();
        } catch (SQLException e) {
            Log.msg("ERROR readLastExecutedSQL: " + e.getMessage());
            this.connection = resetConnection();
        }
        return SQListToBeProcessed;
    }

    private ArrayList<SQL> readLastExecutedSQLFromFile() {
        List<String> files = this.readFiles(Config.getProperty("folderInputWorkload") + Config.getProperty("sgbd"));
        for (String currentQuery : files) {
            if (this.isQueryValid(currentQuery)) {

                Calendar calendar = Calendar.getInstance();
                Date now = calendar.getTime();

                SQL sql = SQLImp.createSQL(currentQuery, new Timestamp(now.getTime()), schema, SQListToBeProcessed);
                if (sql.isValid() && !SQListToBeProcessed.contains(sql)) {
                    SQListToBeProcessed.add(sql);
                }
            }
        }
        return SQListToBeProcessed;
    }

    public List<String> readFiles(String folderName) {
        List<String> lista = new ArrayList<>();
        File folder = new File(folderName);
        File afile[] = folder.listFiles();
        int i = 0;
        for (int j = afile.length; i < j; i++) {
            File file = afile[i];
            if (!file.isDirectory()) {
                String nome = file.getName();
                String extensao = nome.substring(nome.length() - 4);
                switch (extensao) {
                    case ".sql":
                    case ".xml":
                        String[] queries = Log.readFile(folderName + File.separator + nome).split(";");
                        for (String query : queries) {
                            lista.add(query);
                        }
                        break;
                }
            }
        }
        return lista;
    }

    protected boolean isQueryValid(String query) {
        boolean isValid = true;
        if ((this.isQueryGeneratedByThisTool(query))
                || (this.isSQLGeneratedBySGBD(query))
                || (this.isNoQuerySelect(query))) {
            isValid = false;
        }
        return isValid;
    }

    protected boolean isQueryValidSubQuery(String query) {
        boolean isValid = false;
        if ((this.isQueryValid(query))
                && (this.countSelect(query) == 1)) {
            isValid = true;
        }
        return isValid;

    }

    private boolean isSQLGeneratedBySGBD(String query) {
        boolean isCommand = false;
        String[] wordsBySGBD = Config.getProperty("wordsBySGBD").split(";");
        for (String word : wordsBySGBD) {
            if (query.toLowerCase(Locale.getDefault()).contains(word)) {
                isCommand = true;
            }
        }
        return isCommand;
    }

    private boolean isQueryGeneratedByThisTool(String query) {
        return query.toLowerCase().contains(Config.getProperty("signature").toLowerCase());
    }

    private boolean isNoQuerySelect(String query) {
        return !query.toLowerCase(Locale.getDefault()).contains("select");
    }

    protected int countSelect(String query) {
        String queryTemp = query.toLowerCase();
        String findStr = "select";
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {
            lastIndex = queryTemp.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }

}
