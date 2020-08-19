/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.dao;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author rpoat
 */
public class SelectionHeuristicDAO extends BasicDAO {

    public void generateTestFilesSelectionHeuristicPartialIndex() {
        try {
            String queryTemp = Config.getProperty("querySelectionHeuristicPartialIndex_" + Config.getProperty("sgbd"));
            PreparedStatement preparedStatement = connection().prepareStatement(queryTemp);
            preparedStatement.setDouble(1, Double.valueOf(Config.getProperty("selevityMoreThan")));
            ResultSet result = preparedStatement.executeQuery();
            String nameFile = Config.getProperty("folderTuningAction") + File.separatorChar + "all_selected_tuning_actions.sql";
            File file = new File(nameFile);
            if (file.exists()) {
                file.delete();
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(nameFile, true), "UTF-8");
            BufferedWriter fbw = new BufferedWriter(writer);
            while (result.next()) {
                fbw.write(result.getString("tna_ddl"));
                fbw.newLine();
            }
            fbw.close();
            result.close();
            preparedStatement.close();
        } catch (SQLException | IOException ex) {
            Log.error(ex.getMessage());
        }
    }
}
